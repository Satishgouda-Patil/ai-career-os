package com.ai.career.execution.provider;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import com.ai.career.browser.core.BrowserSessionFactory;
import com.ai.career.browser.interaction.*;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.execution.gate.FinalSubmissionGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenericFormApplicationProvider implements ApplicationExecutionProvider {

    public static final String PROVIDER_NAME = "GENERIC_JOB_FORM";

    private final ApplicationRepository applicationRepository;
    private final BrowserUrlValidator urlValidator;
    private final BrowserSessionFactory sessionFactory;
    private final BrowserInteractionService interactionService;
    private final BrowserFormInteractor interactor;
    private final FinalSubmissionGate submissionGate;

    @Override
    public boolean supports(Application application) {
        if (application == null || application.getJob() == null || application.getJob().getUrl() == null) {
            return false;
        }
        String provider = application.getProviderName();
        if ("GENERIC_JOB_FORM".equalsIgnoreCase(provider) || "BROWSER".equalsIgnoreCase(provider) || "LIVE".equalsIgnoreCase(provider)) {
            try {
                urlValidator.validateUrl(application.getJob().getUrl());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return ProviderCapabilities.builder()
                .capabilities(Set.of(
                        ProviderCapability.FORM_APPLICATION,
                        ProviderCapability.RESUME_UPLOAD,
                        ProviderCapability.TEXT_FIELDS,
                        ProviderCapability.SELECT_FIELDS
                ))
                .supportsDryRun(true)
                .supportsFormDiscovery(true)
                .supportsDocumentUpload(true)
                .supportsVerification(true)
                .build();
    }

    @Override
    public ExecutionValidationResult validate(Application application, ExecutionContext context) {
        if (application == null || application.getJob() == null || application.getJob().getUrl() == null) {
            return ExecutionValidationResult.builder()
                    .valid(false)
                    .validationErrors(Collections.singletonList("Invalid application or job URL"))
                    .errorMessage("Invalid application or job URL")
                    .build();
        }
        return ExecutionValidationResult.builder()
                .valid(true)
                .build();
    }

    @Override
    public ExecutionResult execute(Application application, ExecutionContext context) {
        String token = context != null && context.getApplicationAnswers() != null
                ? context.getApplicationAnswers().getOrDefault("confirmationToken", "SUBMIT_APPLICATION")
                : "SUBMIT_APPLICATION";
        return execute(application.getId(), token);
    }

    public ExecutionResult execute(Long applicationId, String confirmationToken) {
        log.info("Executing real application submission for ID: {} via provider: {}", applicationId, getProviderName());

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        String jobUrl = application.getJob().getUrl();
        urlValidator.validateUrl(jobUrl);

        BrowserInteractionPlan plan = interactionService.prepareInteractionPlan(applicationId);
        SubmissionPreview preview = interactionService.executeInteraction(applicationId);

        // Final Submission Gate
        FinalSubmissionGate.GateResult gateResult = submissionGate.verifySubmissionEligibility(application, plan, preview, confirmationToken);

        if (!gateResult.approved()) {
            log.warn("Submission rejected by FinalSubmissionGate: {} ({})", gateResult.errorCode(), gateResult.reason());
            application.setStatus(ApplicationState.SUBMISSION_REQUIRES_REVIEW);
            applicationRepository.save(application);

            return ExecutionResult.builder()
                    .providerName(getProviderName())
                    .status(ExecutionOutcomeStatus.FAILED)
                    .errorCode(gateResult.errorCode())
                    .errorMessage("Submission rejected by safety gate: " + gateResult.reason())
                    .executedAt(LocalDateTime.now())
                    .build();
        }

        // State Transition: APPLYING
        application.setStatus(ApplicationState.APPLYING);
        applicationRepository.save(application);

        String extAppId = UUID.randomUUID().toString();
        try (BrowserSession session = sessionFactory.createSession()) {
            session.navigate(jobUrl);

            // Execute safe interactions
            for (InteractionFieldAction action : plan.getActions()) {
                if (action.getActionType() == InteractionActionType.FILL_TEXT) {
                    interactor.fillText(session, action.getSelector(), action.getValue());
                } else if (action.getActionType() == InteractionActionType.SELECT_OPTION) {
                    interactor.selectOption(session, action.getSelector(), action.getValue());
                } else if (action.getActionType() == InteractionActionType.CHECK) {
                    interactor.checkCheckbox(session, action.getSelector(), true);
                }
            }

            // SINGLE SUBMIT ACTION
            BrowserPage page = session.getPage();
            page.evaluate("(() => { let btn = document.querySelector(\"button[type='submit'], input[type='submit']\"); if (btn) btn.click(); })()");

            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            // Verify confirmation evidence
            String content = page.content();
            boolean confirmationFound = content != null && (content.toLowerCase().contains("thank you")
                    || content.toLowerCase().contains("application received")
                    || content.toLowerCase().contains("submitted successfully"));

            if (confirmationFound) {
                log.info("Application submission verified successfully for ID: {}", applicationId);
                application.setStatus(ApplicationState.APPLIED);
                applicationRepository.save(application);

                return ExecutionResult.builder()
                        .providerName(getProviderName())
                        .status(ExecutionOutcomeStatus.SUCCESS)
                        .externalApplicationId(extAppId)
                        .executedAt(LocalDateTime.now())
                        .build();
            } else {
                log.warn("Submission dispatched but confirmation could not be verified automatically for ID: {}", applicationId);
                application.setStatus(ApplicationState.SUBMISSION_REQUIRES_REVIEW);
                applicationRepository.save(application);

                return ExecutionResult.builder()
                        .providerName(getProviderName())
                        .status(ExecutionOutcomeStatus.REQUIRES_HUMAN)
                        .errorCode("CONFIRMATION_UNVERIFIED")
                        .errorMessage("Submit dispatched but confirmation evidence not found")
                        .executedAt(LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            log.error("Execution failed for application ID {}: {}", applicationId, e.getMessage(), e);
            application.setStatus(ApplicationState.FAILED);
            applicationRepository.save(application);

            return ExecutionResult.builder()
                    .providerName(getProviderName())
                    .status(ExecutionOutcomeStatus.FAILED)
                    .errorCode("EXECUTION_ERROR")
                    .errorMessage("Execution exception: " + e.getMessage())
                    .executedAt(LocalDateTime.now())
                    .build();
        }
    }
}
