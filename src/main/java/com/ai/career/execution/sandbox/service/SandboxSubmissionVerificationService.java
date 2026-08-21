package com.ai.career.execution.sandbox.service;

import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SandboxSubmissionVerificationService {

    public boolean verifySandboxExecution(SandboxExecutionResultDto result) {
        if (result == null) {
            log.warn("Sandbox verification failed: result is null");
            return false;
        }

        if (!"SANDBOX".equalsIgnoreCase(result.getExecutionMode())) {
            log.warn("Sandbox verification failed: invalid execution mode '{}'", result.getExecutionMode());
            return false;
        }

        if (result.isRealSubmissionAttempted()) {
            log.error("CRITICAL SAFETY VIOLATION: realSubmissionAttempted is TRUE during sandbox verification!");
            return false;
        }

        if (result.isEmailSent()) {
            log.error("CRITICAL SAFETY VIOLATION: emailSent is TRUE during sandbox verification!");
            return false;
        }

        if (result.isFileUploadedToRealProvider()) {
            log.error("CRITICAL SAFETY VIOLATION: fileUploadedToRealProvider is TRUE during sandbox verification!");
            return false;
        }

        if (!result.isSubmissionSimulated()) {
            log.warn("Sandbox verification failed: submissionSimulated is false");
            return false;
        }

        log.info("Sandbox submission verification passed 100% for application ID: {}", result.getApplicationId());
        return true;
    }
}
