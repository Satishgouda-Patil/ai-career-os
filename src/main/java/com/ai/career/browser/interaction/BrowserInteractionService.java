package com.ai.career.browser.interaction;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import com.ai.career.browser.core.BrowserSessionFactory;
import com.ai.career.browser.discovery.*;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.form.model.FieldType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public SubmissionPreview executeInteraction(Long applicationId) {
        Application application = getValidApplication(applicationId);
        String jobUrl = application.getJob().getUrl();
        urlValidator.validateUrl(jobUrl);

        BrowserInteractionPlan plan = prepareInteractionPlan(applicationId);
        Map<String, String> filledFields = new HashMap<>();
        List<String> uploadedFiles = new ArrayList<>();
        List<String> warnings = new ArrayList<>(plan.getWarnings());
        boolean submitControlDetected = false;

        try (BrowserSession session = sessionFactory.createSession()) {
            session.navigate(jobUrl);

            BrowserPage page = session.getPage();

            // Detect submit controls without interacting
            List<BrowserPage.ElementHandle> submitButtons = page.querySelectorAll("button[type='submit'], input[type='submit'], button:has-text('Submit'), button:has-text('Apply')");
            if (!submitButtons.isEmpty()) {
                submitControlDetected = true;
            }

            for (InteractionFieldAction action : plan.getActions()) {
                if (action.getActionType() == InteractionActionType.FILL_TEXT) {
                    interactor.fillText(session, action.getSelector(), action.getValue());
                    filledFields.put(action.getFieldName(), action.getValue());
                } else if (action.getActionType() == InteractionActionType.SELECT_OPTION) {
                    interactor.selectOption(session, action.getSelector(), action.getValue());
                    filledFields.put(action.getFieldName(), action.getValue());
                } else if (action.getActionType() == InteractionActionType.CHECK) {
                    interactor.checkCheckbox(session, action.getSelector(), true);
                    filledFields.put(action.getFieldName(), "true");
                } else if (action.getActionType() == InteractionActionType.UPLOAD_FILE) {
                    uploadedFiles.add(action.getValue());
                }
            }

            return SubmissionPreview.builder()
                    .applicationId(applicationId)
                    .formId(plan.getFormId())
                    .submitControlDetected(submitControlDetected)
                    .readyForSubmission(false) // ALWAYS false in M6-B
                    .filledFields(filledFields)
                    .uploadedFiles(uploadedFiles)
                    .unresolvedFields(plan.getUnresolvedFields())
                    .warnings(warnings)
                    .status("CONTROLLED_INTERACTION_VERIFIED_STOPPED")
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
                || combined.contains("race") || combined.contains("ethnicity") || combined.contains("salary");
    }
}
