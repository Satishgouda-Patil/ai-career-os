package com.ai.career.execution.sandbox;

import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import com.ai.career.execution.sandbox.service.SandboxSubmissionVerificationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SandboxSubmissionVerificationServiceTest {

    private final SandboxSubmissionVerificationService verificationService = new SandboxSubmissionVerificationService();

    @Test
    void testAllVerificationChecksPass() {
        SandboxExecutionResultDto dto = SandboxExecutionResultDto.builder()
                .applicationId(100L)
                .executionMode("SANDBOX")
                .status("VERIFIED")
                .submissionSimulated(true)
                .submissionVerified(true)
                .realSubmissionAttempted(false)
                .emailSent(false)
                .fileUploadedToRealProvider(false)
                .build();

        boolean verified = verificationService.verifySandboxExecution(dto);

        assertThat(verified).isTrue();
    }

    @Test
    void testVerificationFailsOnRealSubmissionViolation() {
        SandboxExecutionResultDto dto = SandboxExecutionResultDto.builder()
                .applicationId(100L)
                .executionMode("SANDBOX")
                .submissionSimulated(true)
                .realSubmissionAttempted(true) // CRITICAL VIOLATION
                .build();

        boolean verified = verificationService.verifySandboxExecution(dto);

        assertThat(verified).isFalse();
    }

    @Test
    void testVerificationFailsOnEmailSentViolation() {
        SandboxExecutionResultDto dto = SandboxExecutionResultDto.builder()
                .applicationId(100L)
                .executionMode("SANDBOX")
                .submissionSimulated(true)
                .emailSent(true) // CRITICAL VIOLATION
                .build();

        boolean verified = verificationService.verifySandboxExecution(dto);

        assertThat(verified).isFalse();
    }
}
