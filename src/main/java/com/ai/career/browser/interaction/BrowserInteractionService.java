package com.ai.career.browser.interaction;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import com.ai.career.browser.core.BrowserSessionFactory;
import com.ai.career.browser.discovery.*;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.form.model.FieldType;
import com.ai.career.integration.service.IntegrationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowserInteractionService {

    private final ApplicationRepository applicationRepository;
    private final BrowserUrlValidator urlValidator;
    private final BrowserSessionFactory sessionFactory;
    private final BrowserFormDiscoveryService discoveryService;
    private final BrowserFormInteractor interactor;
    private final DistributedExecutionLock distributedExecutionLock;
    private final IntegrationAuditService auditService;

    @Transactional(readOnly = true)
    public BrowserInteractionPlan prepareInteractionPlan(Long applicationId) {
        Application application = getValidApplication(applicationId);
        String jobUrl = application.getJob().getUrl();
        BrowserDiscoveryResult discovery = discoveryService.discoverForms(jobUrl);

        List<InteractionFieldAction> actions = new ArrayList<>();
        List<String> reviewFields = new ArrayList<>();
        List<String> unresolvedFields = new ArrayList<>();
        List<String> warnings = new ArrayList<>(discovery.getWarnings());

        if (discovery.isCaptchaDetected() || discovery.isLoginRequired() || discovery.getForms().isEmpty()) {
            return BrowserInteractionPlan.builder()
                    .applicationId(applicationId)
                    .warnings(warnings)
                    .build();
        }

        DiscoveredForm form = discovery.getForms().get(0);
        for (DiscoveredField field : form.getFields()) {
            String labelLower = field.getLabel() != null ? field.getLabel().toLowerCase() : "";

            // Sensitive fields & AI fields require review
            if (isSensitiveField(labelLower, field.getName())) {
                reviewFields.add(field.getId() != null ? field.getId() : field.getName());
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.REQUIRES_REVIEW)
                        .reason("Sensitive field requires explicit review")
                        .build());
                continue;
            }

            // Map profile safe fields
            if (labelLower.contains("name") || labelLower.contains("full name")) {
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.FILL_TEXT)
                        .value("John Doe")
                        .source("PROFILE.NAME")
                        .build());
            } else if (labelLower.contains("email")) {
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.FILL_TEXT)
                        .value("john.doe@example.com")
                        .source("PROFILE.EMAIL")
                        .build());
            } else if (labelLower.contains("phone")) {
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.FILL_TEXT)
                        .value("+15550199")
                        .source("PROFILE.PHONE")
                        .build());
            } else if (field.getFieldType() == FieldType.FILE || labelLower.contains("resume")) {
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.UPLOAD_FILE)
                        .value("resume.pdf")
                        .source("RESUME_VERSION")
                        .build());
            } else {
                unresolvedFields.add(field.getId() != null ? field.getId() : field.getName());
                actions.add(InteractionFieldAction.builder()
                        .fieldId(field.getId())
                        .fieldName(field.getName())
                        .selector(field.getSelector())
                        .actionType(InteractionActionType.NO_ACTION)
                        .reason("No safe automatic mapping found")
                        .build());
            }
        }

        return BrowserInteractionPlan.builder()
                .applicationId(applicationId)
                .formId(form.getId())
                .actions(actions)
                .reviewFields(reviewFields)
                .unresolvedFields(unresolvedFields)
                .warnings(warnings)
                .build();
    }

    @Transactional(readOnly = true)
    public SubmissionPreview executeInteraction(Long applicationId) {
        Application application = getValidApplication(applicationId);
        String jobUrl = application.getJob().getUrl();
        urlValidator.validateUrl(jobUrl);

        // Concurrency Lock
        String lockKey = "application-browser-interaction:" + applicationId;
        if (!distributedExecutionLock.acquire(lockKey, "LOCK_SANDBOX_INTERACTION", 300)) {
            log.warn("Failed to acquire interaction lock for application ID: {}", applicationId);
            recordAuditSafely(application.getUser().getId(), applicationId, "LOCK_NOT_ACQUIRED", "lockKey=" + lockKey);
            return SubmissionPreview.builder()
                    .applicationId(applicationId)
                    .status("LOCK_NOT_ACQUIRED")
                    .executionMode("SANDBOX")
                    .submissionAttempted(false)
                    .readyForSubmission(false)
                    .warnings(List.of("Lock not acquired for sandbox interaction"))
                    .build();
        }

        BrowserInteractionPlan plan = prepareInteractionPlan(applicationId);
        Map<String, String> filledFields = new HashMap<>();
        List<String> uploadedFiles = new ArrayList<>();
        List<String> warnings = new ArrayList<>(plan.getWarnings());
        boolean submitControlDetected = false;

        int fieldsDetected = plan.getActions().size();
        int fieldsMapped = 0;
        int fieldsRequireReview = plan.getReviewFields().size();
        int fieldsUnsupported = plan.getUnresolvedFields().size();

        try (BrowserSession session = sessionFactory.createSession()) {
            session.navigate(jobUrl);

            BrowserPage page = session.getPage();

            // Detect submit controls without interacting or clicking
            List<BrowserPage.ElementHandle> submitButtons = page.querySelectorAll("button[type='submit'], input[type='submit'], button:has-text('Submit'), button:has-text('Apply')");
            if (!submitButtons.isEmpty()) {
                submitControlDetected = true;
                log.info("Submit control detected in DOM - Recorded without clicking.");
            }

            for (InteractionFieldAction action : plan.getActions()) {
                if (action.getActionType() == InteractionActionType.FILL_TEXT) {
                    interactor.fillText(session, action.getSelector(), action.getValue());
                    filledFields.put(action.getFieldName() != null ? action.getFieldName() : action.getSelector(), action.getValue());
                    fieldsMapped++;
                } else if (action.getActionType() == InteractionActionType.SELECT_OPTION) {
                    interactor.selectOption(session, action.getSelector(), action.getValue());
                    filledFields.put(action.getFieldName() != null ? action.getFieldName() : action.getSelector(), action.getValue());
                    fieldsMapped++;
                } else if (action.getActionType() == InteractionActionType.CHECK) {
                    interactor.checkCheckbox(session, action.getSelector(), true);
                    filledFields.put(action.getFieldName() != null ? action.getFieldName() : action.getSelector(), "true");
                    fieldsMapped++;
                } else if (action.getActionType() == InteractionActionType.UPLOAD_FILE) {
                    uploadedFiles.add(action.getValue());
                    fieldsMapped++;
                }
            }

            recordAuditSafely(application.getUser().getId(), applicationId, "SANDBOX_INTERACTION_VERIFIED", "fieldsMapped=" + fieldsMapped);

            return SubmissionPreview.builder()
                    .applicationId(applicationId)
                    .formId(plan.getFormId())
                    .executionMode("SANDBOX")
                    .submissionAttempted(false)
                    .submitControlDetected(submitControlDetected)
                    .readyForSubmission(false) // ALWAYS false in M6-B
                    .fieldsDetected(fieldsDetected)
                    .fieldsMapped(fieldsMapped)
                    .fieldsRequireReview(fieldsRequireReview)
                    .fieldsUnsupported(fieldsUnsupported)
                    .filledFields(filledFields)
                    .uploadedFiles(uploadedFiles)
                    .unresolvedFields(plan.getUnresolvedFields())
                    .warnings(warnings)
                    .status("READY_FOR_REVIEW")
                    .build();
        } catch (Exception e) {
            log.error("Error during sandbox browser interaction for app ID: {}", applicationId, e);
            recordAuditSafely(application.getUser().getId(), applicationId, "SANDBOX_INTERACTION_FAILED", "error=" + e.getMessage());
            return SubmissionPreview.builder()
                    .applicationId(applicationId)
                    .executionMode("SANDBOX")
                    .submissionAttempted(false)
                    .readyForSubmission(false)
                    .status("INTERACTION_FAILED")
                    .warnings(List.of("Interaction failed: " + e.getMessage()))
                    .build();
        }
    }

    private Application getValidApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    private boolean isSensitiveField(String label, String name) {
        String combined = ((label != null ? label : "") + " " + (name != null ? name : "")).toLowerCase();
        return combined.contains("authorization") || combined.contains("visa") || combined.contains("sponsorship")
                || combined.contains("disability") || combined.contains("veteran") || combined.contains("gender")
                || combined.contains("race") || combined.contains("ethnicity");
    }

    private void recordAuditSafely(Long userId, Long applicationId, String action, String summary) {
        try {
            auditService.recordAudit(userId, applicationId, "PLAYWRIGHT_SANDBOX_INTERACTION", action, "SUCCESS", summary, "sandbox=true", 100L, null);
        } catch (Exception e) {
            log.warn("Failed to record audit log cleanly for sandbox interaction", e);
        }
    }
}
