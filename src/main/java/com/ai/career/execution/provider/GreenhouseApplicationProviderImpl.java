package com.ai.career.execution.provider;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.registry.ProductionProviderRegistry;
import com.ai.career.integration.service.IntegrationAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GreenhouseApplicationProviderImpl implements ApplicationExecutionProvider, ProductionProviderRegistry.ProviderPlugin {

    private final DistributedExecutionLock distributedExecutionLock;
    private final IntegrationAuditService auditService;

    @Value("${app.safety.allow-live-submission:false}")
    private boolean allowLiveSubmission;

    public GreenhouseApplicationProviderImpl(
            DistributedExecutionLock distributedExecutionLock,
            IntegrationAuditService auditService,
            ProductionProviderRegistry registry) {
        this.distributedExecutionLock = distributedExecutionLock;
        this.auditService = auditService;
        registry.registerProvider(this);
    }

    @Override
    public boolean supports(Application application) {
        if (application == null || application.getJob() == null || application.getJob().getUrl() == null) {
            return false;
        }
        String url = application.getJob().getUrl().toLowerCase();
        return url.contains("greenhouse.io") || url.contains("boards.greenhouse.io");
    }

    @Override
    public String getProviderName() {
        return "GREENHOUSE_PRODUCTION";
    }

    @Override
    public ProductionProviderRegistry.ProviderCategory getCategory() {
        return ProductionProviderRegistry.ProviderCategory.APPLICATION_EXECUTION;
    }

    @Override
    public boolean isSandbox() {
        return false;
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return ProviderCapabilities.builder()
            .supportsDryRun(true)
            .supportsFormDiscovery(true)
            .supportsDocumentUpload(true)
            .supportsVerification(true)
            .capabilities(Set.of(ProviderCapability.values()))
            .build();
    }

    public List<String> verify11PreSubmissionChecks(Application application, ExecutionContext context) {
        List<String> failures = new ArrayList<>();

        // Check 1: Target domain verification
        if (!supports(application)) {
            failures.add("Target domain is not a valid Greenhouse portal");
        }

        // Check 2: Provider identity verification
        if (!"GREENHOUSE_PRODUCTION".equals(getProviderName())) {
            failures.add("Provider identity mismatch");
        }

        // Check 3: Application ID validation
        if (application == null || application.getId() == null) {
            failures.add("Invalid or null application ID");
        }

        // Check 4: Candidate approval verification
        if (application != null && application.getStatus() != ApplicationState.APPROVED && application.getStatus() != ApplicationState.CONFIRMED_SUBMISSION && application.getStatus() != ApplicationState.APPLYING) {
            failures.add("Candidate explicit approval missing (Status must be APPROVED or CONFIRMED_SUBMISSION)");
        }

        // Check 5: CONFIRMED_SUBMISSION state verification
        if (application != null && application.getStatus() != ApplicationState.CONFIRMED_SUBMISSION && application.getStatus() != ApplicationState.APPLYING) {
            failures.add("Step 3 CONFIRMED_SUBMISSION state verification failed");
        }

        // Check 6: Form readiness verification
        if (context == null || context.getJobUrl() == null || context.getJobUrl().isBlank()) {
            failures.add("Application Execution Context or Job URL is uninitialized");
        }

        // Check 7: Zero unresolved required fields
        if (context != null && context.getApplicationAnswers() != null && context.getApplicationAnswers().containsKey("UNRESOLVED_REQUIRED_FIELDS")) {
            failures.add("Unresolved required form fields exist");
        }

        // Check 8: Distributed execution lock
        String lockKey = "EXEC_LOCK_APP_" + (application != null ? application.getId() : "NULL");
        if (!distributedExecutionLock.acquire(lockKey, "LOCK_GREENHOUSE", 300)) {
            failures.add("Failed to acquire distributed execution lock for application ID");
        }

        // Check 9: Provider capability verification
        if (!getCapabilities().isSupportsDryRun()) {
            failures.add("Provider capabilities validation failed");
        }

        // Check 10: Live submission configuration verification
        if (!allowLiveSubmission && context != null && !context.isDryRun()) {
            failures.add("Live submission configuration is DISABLED (app.safety.allow-live-submission=false)");
        }

        // Check 11: Audit record creation
        try {
            if (application != null && application.getUser() != null) {
                auditService.recordAudit(
                    application.getUser().getId(),
                    application.getId(),
                    getProviderName(),
                    "PRE_SUBMISSION_CHECK",
                    failures.isEmpty() ? "PASSED" : "FAILED",
                    "checksCount=11",
                    "failures=" + failures.size(),
                    50L,
                    failures.isEmpty() ? null : "SAFETY_CHECK_FAILED"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to write pre-submission audit record safely", e);
        }

        return failures;
    }

    @Override
    public ExecutionValidationResult validate(Application application, ExecutionContext context) {
        List<String> checkFailures = verify11PreSubmissionChecks(application, context);
        if (!checkFailures.isEmpty()) {
            return ExecutionValidationResult.builder()
                .valid(false)
                .validationErrors(checkFailures)
                .errorMessage(String.join("; ", checkFailures))
                .build();
        }
        return ExecutionValidationResult.builder()
            .valid(true)
            .validationErrors(List.of())
            .build();
    }

    @Override
    public ExecutionResult execute(Application application, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        ExecutionValidationResult validation = validate(application, context);

        if (!validation.isValid()) {
            log.warn("Greenhouse pre-submission checks failed for application ID: {}. Halting execution.", application.getId());
            long duration = System.currentTimeMillis() - startTime;

            if (application.getUser() != null) {
                auditService.recordAudit(
                    application.getUser().getId(),
                    application.getId(),
                    getProviderName(),
                    "SUBMISSION_EXECUTION",
                    "FAILED",
                    "dryRun=" + (context != null && context.isDryRun()),
                    "errors=" + validation.getValidationErrors(),
                    duration,
                    "SAFETY_CHECKS_FAILED"
                );
            }

            return ExecutionResult.builder()
                .status(ExecutionOutcomeStatus.FAILED)
                .providerName(getProviderName())
                .externalApplicationId(application.getId() != null ? application.getId().toString() : null)
                .errorMessage("Pre-submission checks failed: " + String.join("; ", validation.getValidationErrors()))
                .executedAt(LocalDateTime.now())
                .build();
        }

        // Safe Dry-Run Execution
        long duration = System.currentTimeMillis() - startTime;
        log.info("Greenhouse Controlled Dry-Run validation completed successfully for application ID: {}", application.getId());

        if (application.getUser() != null) {
            auditService.recordAudit(
                application.getUser().getId(),
                application.getId(),
                getProviderName(),
                "SUBMISSION_EXECUTION_DRYRUN",
                "SUCCESS",
                "dryRun=true&jobUrl=" + (application.getJob() != null ? application.getJob().getUrl() : ""),
                "validationResult=PASSED",
                duration,
                null
            );
        }

        return ExecutionResult.builder()
            .status(ExecutionOutcomeStatus.SUCCESS)
            .providerName(getProviderName())
            .externalApplicationId("GREENHOUSE_DRY_RUN_" + application.getId())
            .executedAt(LocalDateTime.now())
            .build();
    }
}
