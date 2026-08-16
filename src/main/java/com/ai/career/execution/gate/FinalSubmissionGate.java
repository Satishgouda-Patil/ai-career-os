package com.ai.career.execution.gate;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.browser.interaction.BrowserInteractionPlan;
import com.ai.career.browser.interaction.SubmissionPreview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FinalSubmissionGate {

    public GateResult verifySubmissionEligibility(Application application, BrowserInteractionPlan plan, SubmissionPreview preview, String confirmationToken) {
        log.info("Evaluating FinalSubmissionGate for application ID: {}", application.getId());

        if (!"SUBMIT_APPLICATION".equals(confirmationToken)) {
            return GateResult.fail("INVALID_CONFIRMATION_TOKEN", "Confirmation token must be SUBMIT_APPLICATION");
        }

        if (application.getJob() == null || application.getJob().getUrl() == null) {
            return GateResult.fail("INVALID_JOB_URL", "Job URL is missing or invalid");
        }

        if (plan.getWarnings().contains("CAPTCHA_DETECTED")) {
            return GateResult.fail("CAPTCHA_DETECTED", "CAPTCHA detected; automated submission blocked");
        }

        if (plan.getWarnings().contains("LOGIN_REQUIRED")) {
            return GateResult.fail("LOGIN_REQUIRED", "Login required; automated submission blocked");
        }

        if (!plan.getReviewFields().isEmpty()) {
            return GateResult.fail("SENSITIVE_FIELDS_UNREVIEWED", "Sensitive fields require explicit user review: " + plan.getReviewFields());
        }

        if (!plan.getUnresolvedFields().isEmpty()) {
            return GateResult.fail("UNRESOLVED_FIELDS_EXIST", "Unresolved fields remain: " + plan.getUnresolvedFields());
        }

        if (!preview.isSubmitControlDetected()) {
            return GateResult.fail("SUBMIT_CONTROL_MISSING", "No submit button control found on page");
        }

        log.info("FinalSubmissionGate passed cleanly for application ID: {}", application.getId());
        return GateResult.pass();
    }

    public record GateResult(boolean approved, String errorCode, String reason) {
        public static GateResult pass() {
            return new GateResult(true, null, "Approved for live submission");
        }

        public static GateResult fail(String errorCode, String reason) {
            return new GateResult(false, errorCode, reason);
        }
    }
}
