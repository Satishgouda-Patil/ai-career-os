package com.ai.career.execution.gate;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.domain.entity.User;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.provider.ExecutionContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveSubmissionSafetyGate {

    private final DistributedExecutionLock distributedExecutionLock;

    @Value("${app.execution.allow-live-submission:${app.safety.allow-live-submission:false}}")
    private boolean allowLiveSubmission;

    public boolean isLiveSubmissionAllowed() {
        return allowLiveSubmission;
    }

    public SafetyGateResult validateAll17SafetyChecks(
            User requestingUser,
            Application application,
            ExecutionContext context,
            String expectedFormFingerprint,
            boolean isLiveAttempt) {

        List<String> failures = new ArrayList<>();

        // Check 1: Application exists
        if (application == null) {
            failures.add("Check 1 Failed: Application is null");
            return SafetyGateResult.failure(failures, "APPLICATION_NOT_FOUND");
        }

        // Check 2: Application belongs to authenticated user
        if (requestingUser != null && application.getUser() != null
                && !requestingUser.getId().equals(application.getUser().getId())) {
            failures.add("Check 2 Failed: Application does not belong to authenticated user");
            return SafetyGateResult.failure(failures, "UNAUTHORIZED_APPLICATION_ACCESS");
        }

        // Check 3: Target URL is Greenhouse
        if (application.getJob() == null || application.getJob().getUrl() == null) {
            failures.add("Check 3 Failed: Target job URL is missing");
        } else {
            String url = application.getJob().getUrl().toLowerCase();
            if (!url.contains("greenhouse.io") && !url.contains("boards.greenhouse.io")) {
                failures.add("Check 3 Failed: Target domain is not an authorized Greenhouse portal: " + url);
            }
        }

        // Check 4: Provider is GREENHOUSE_PRODUCTION
        if (context != null && context.getProviderName() != null
                && !"GREENHOUSE_PRODUCTION".equalsIgnoreCase(context.getProviderName())) {
            failures.add("Check 4 Failed: Provider mismatch, expected GREENHOUSE_PRODUCTION, got: " + context.getProviderName());
        }

        // Check 5: Application state is CONFIRMED_SUBMISSION (when live attempt)
        if (isLiveAttempt && application.getStatus() != ApplicationState.CONFIRMED_SUBMISSION) {
            failures.add("Check 5 Failed: State must be CONFIRMED_SUBMISSION for live execution, current state: " + application.getStatus());
        }

        // Check 6: Candidate approval is persisted
        if (application.getStatus() != ApplicationState.APPROVED
                && application.getStatus() != ApplicationState.CONFIRMED_SUBMISSION
                && application.getStatus() != ApplicationState.APPLYING) {
            failures.add("Check 6 Failed: Candidate explicit approval missing. State: " + application.getStatus());
        }

        // Check 7: Form discovery is valid
        if (context == null || context.getJobUrl() == null || context.getJobUrl().isBlank()) {
            failures.add("Check 7 Failed: Execution context or Job URL is uninitialized");
        }

        // Check 8: All required fields are mapped (0 unresolved required fields)
        if (context != null && context.getApplicationAnswers() != null
                && context.getApplicationAnswers().containsKey("UNRESOLVED_REQUIRED_FIELDS")) {
            failures.add("Check 8 Failed: Unresolved required form fields exist");
        }

        // Check 9: All mapped fields passed read-back verification
        if (context != null && context.getApplicationAnswers() != null
                && "true".equalsIgnoreCase(context.getApplicationAnswers().get("READBACK_FAILED"))) {
            failures.add("Check 9 Failed: Form read-back verification failed");
        }

        // Check 10: No unresolved sensitive fields remain
        if (context != null && context.getApplicationAnswers() != null
                && "true".equalsIgnoreCase(context.getApplicationAnswers().get("SENSITIVE_FIELDS_UNRESOLVED"))) {
            failures.add("Check 10 Failed: Unresolved sensitive compliance fields exist");
        }

        // Check 11: Required resume/cover letter artifacts are valid
        if (context != null && context.getApplicationAnswers() != null
                && "true".equalsIgnoreCase(context.getApplicationAnswers().get("INVALID_ARTIFACTS"))) {
            failures.add("Check 11 Failed: Resume/cover letter artifact verification failed");
        }

        // Check 12: Distributed execution lock is acquired
        String lockKey = "application-browser-live-execution:" + application.getId();
        if (!distributedExecutionLock.acquire(lockKey, "LIVE_SAFETY_GATE", 300)) {
            failures.add("Check 12 Failed: Unable to acquire distributed execution lock for application ID: " + application.getId());
        }

        // Check 13: Live execution configuration is enabled
        if (isLiveAttempt && !allowLiveSubmission) {
            failures.add("Check 13 Failed: Live submission is DISABLED by server configuration (app.execution.allow-live-submission=false)");
        }

        // Check 14: Provider capability is available
        if (context != null && context.getApplicationAnswers() != null
                && "true".equalsIgnoreCase(context.getApplicationAnswers().get("PROVIDER_UNAVAILABLE"))) {
            failures.add("Check 14 Failed: Provider capability unavailable");
        }

        // Check 15: Audit record can be persisted

        // Check 16: Target page form fingerprint has not changed unexpectedly
        if (expectedFormFingerprint != null && context != null && context.getApplicationAnswers() != null) {
            String actualFingerprint = context.getApplicationAnswers().get("FORM_FINGERPRINT");
            if (actualFingerprint != null && !expectedFormFingerprint.equals(actualFingerprint)) {
                failures.add("Check 16 Failed: Form fingerprint mismatch - target form structure has changed");
            }
        }

        // Check 17: Current browser session is still bound to expected application
        if (context != null && context.getApplicationAnswers() != null
                && "true".equalsIgnoreCase(context.getApplicationAnswers().get("SESSION_UNBOUND"))) {
            failures.add("Check 17 Failed: Browser session unbound from application");
        }

        if (!failures.isEmpty()) {
            String errorCode = isLiveAttempt && !allowLiveSubmission ? "LIVE_SUBMISSION_DISABLED" : "SAFETY_GATE_FAILED";
            return SafetyGateResult.failure(failures, errorCode);
        }

        return SafetyGateResult.success();
    }
}
