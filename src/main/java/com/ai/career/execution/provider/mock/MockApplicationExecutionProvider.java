package com.ai.career.execution.provider.mock;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.execution.provider.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class MockApplicationExecutionProvider implements ApplicationExecutionProvider {

    public static final String PROVIDER_NAME = "MOCK";

    @Getter
    @Setter
    private MockScenario defaultScenario = MockScenario.SUCCESS;

    @Override
    public boolean supports(Application application) {
        if (application == null) {
            return false;
        }
        String provider = application.getProviderName();
        return provider == null || "MOCK".equalsIgnoreCase(provider) || "DIRECT".equalsIgnoreCase(provider);
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
                ProviderCapability.COVER_LETTER_UPLOAD,
                ProviderCapability.TEXT_FIELDS
            ))
            .supportsDryRun(true)
            .supportsFormDiscovery(true)
            .supportsDocumentUpload(true)
            .supportsVerification(true)
            .build();
    }

    @Override
    public ExecutionValidationResult validate(Application application, ExecutionContext context) {
        MockScenario scenario = resolveScenario(context);
        if (scenario == MockScenario.VALIDATION_FAILURE) {
            return ExecutionValidationResult.builder()
                .valid(false)
                .missingFields(Collections.singletonList("phone_number"))
                .validationErrors(Collections.singletonList("Candidate phone number is required"))
                .errorMessage("Validation failed: missing required candidate fields")
                .build();
        }
        return ExecutionValidationResult.builder()
            .valid(true)
            .missingFields(Collections.emptyList())
            .validationErrors(Collections.emptyList())
            .build();
    }

    @Override
    public ExecutionResult execute(Application application, ExecutionContext context) {
        MockScenario scenario = resolveScenario(context);
        log.info("Executing MockApplicationExecutionProvider for Application ID: {} under scenario: {}", application.getId(), scenario);

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mockExecution", true);
        metadata.put("scenario", scenario.name());

        switch (scenario) {
            case SUCCESS:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.SUCCESS)
                    .providerName(PROVIDER_NAME)
                    .externalApplicationId("MOCK-APP-" + application.getId() + "-" + System.currentTimeMillis())
                    .externalUrl("https://mock-portal.example.com/confirmation/" + application.getId())
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case VALIDATION_FAILURE:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.FAILED)
                    .providerName(PROVIDER_NAME)
                    .errorCode("VALIDATION_ERROR")
                    .errorMessage("Form validation failed: missing candidate phone number")
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case REQUIRES_HUMAN:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.REQUIRES_HUMAN)
                    .providerName(PROVIDER_NAME)
                    .errorCode("CAPTCHA_ENCOUNTERED")
                    .errorMessage("CAPTCHA challenge detected on application form. Human intervention required.")
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case UNSUPPORTED:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.UNSUPPORTED)
                    .providerName(PROVIDER_NAME)
                    .errorCode("UNSUPPORTED_FORM_TYPE")
                    .errorMessage("Application form contains unsupported custom widget elements.")
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case RETRYABLE_FAILURE:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.FAILED)
                    .providerName(PROVIDER_NAME)
                    .errorCode("NETWORK_TIMEOUT")
                    .errorMessage("Simulated transient network timeout during submission.")
                    .retryable(true)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case NON_RETRYABLE_FAILURE:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.FAILED)
                    .providerName(PROVIDER_NAME)
                    .errorCode("JOB_EXPIRED")
                    .errorMessage("Simulated external job posting expired.")
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();

            case UNKNOWN:
            default:
                return ExecutionResult.builder()
                    .status(ExecutionOutcomeStatus.UNKNOWN)
                    .providerName(PROVIDER_NAME)
                    .errorCode("SUBMISSION_UNVERIFIED")
                    .errorMessage("Form submission completed but confirmation page could not be verified.")
                    .retryable(false)
                    .metadata(metadata)
                    .executedAt(now)
                    .build();
        }
    }

    private MockScenario resolveScenario(ExecutionContext context) {
        if (context != null && context.getApplicationAnswers() != null && context.getApplicationAnswers().containsKey("mockScenario")) {
            try {
                return MockScenario.valueOf(context.getApplicationAnswers().get("mockScenario"));
            } catch (Exception ignored) {
            }
        }
        return defaultScenario;
    }
}
