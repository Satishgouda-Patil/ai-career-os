package com.ai.career.execution.sandbox.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.sandbox.domain.entity.SandboxExecutionRun;
import com.ai.career.execution.sandbox.domain.repository.SandboxExecutionRunRepository;
import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import com.ai.career.execution.sandbox.provider.SandboxApplicationExecutionProvider;
import com.ai.career.execution.sandbox.service.SandboxExecutionService;
import com.ai.career.execution.sandbox.service.SandboxSubmissionVerificationService;
import com.ai.career.integration.service.IntegrationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxExecutionServiceImpl implements SandboxExecutionService {

    private final ApplicationRepository applicationRepository;
    private final SandboxExecutionRunRepository sandboxRunRepository;
    private final SandboxApplicationExecutionProvider sandboxProvider;
    private final SandboxSubmissionVerificationService verificationService;
    private final DistributedExecutionLock distributedExecutionLock;
    private final IntegrationAuditService auditService;

    @Override
    @Transactional
    public SandboxExecutionResultDto executeSandbox(Long applicationId, Long userId) {
        log.info("Initiating M6-C sandbox execution for application ID: {} by user ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own application ID: " + applicationId);
        }

        // Concurrency Lock
        String lockKey = "application-browser-sandbox:" + applicationId;
        if (!distributedExecutionLock.acquire(lockKey, "LOCK_M6C_SANDBOX", 300)) {
            log.warn("Failed to acquire M6-C sandbox lock for application ID: {}", applicationId);
            recordAuditSafely(userId, applicationId, "LOCK_NOT_ACQUIRED", "lockKey=" + lockKey);
            return SandboxExecutionResultDto.builder()
                    .applicationId(applicationId)
                    .executionMode("SANDBOX")
                    .status("LOCK_NOT_ACQUIRED")
                    .realSubmissionAttempted(false)
                    .emailSent(false)
                    .fileUploadedToRealProvider(false)
                    .build();
        }

        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 1. Execute Sandbox Provider Workflow
            SandboxExecutionResultDto result = sandboxProvider.executeSandboxApplication(applicationId, userId);

            // 2. Perform Submission Verification
            boolean verified = verificationService.verifySandboxExecution(result);
            result.setSubmissionVerified(verified);
            result.setStatus(verified ? "VERIFIED" : "FAILED");

            // 3. Persist Sandbox Execution Run Record
            SandboxExecutionRun run = SandboxExecutionRun.builder()
                    .application(application)
                    .user(application.getUser())
                    .executionMode("SANDBOX")
                    .status(result.getStatus())
                    .fieldsDetected(result.getFieldsDetected())
                    .fieldsMapped(result.getFieldsMapped())
                    .fieldsVerified(result.getFieldsVerified())
                    .fieldsRequireReview(result.getFieldsRequireReview())
                    .submissionSimulated(result.isSubmissionSimulated())
                    .submissionVerified(verified)
                    .realSubmissionAttempted(false)
                    .startedAt(startTime)
                    .completedAt(LocalDateTime.now())
                    .errorCode(result.getErrorCode())
                    .build();

            sandboxRunRepository.save(run);

            // 4. Audit Log
            recordAuditSafely(userId, applicationId, "SANDBOX_EXECUTION_" + result.getStatus(), "verified=" + verified);

            return result;

        } catch (Exception e) {
            log.error("Sandbox execution failed for application ID: {}", applicationId, e);
            recordAuditSafely(userId, applicationId, "SANDBOX_EXECUTION_FAILED", "error=" + e.getMessage());

            SandboxExecutionRun run = SandboxExecutionRun.builder()
                    .application(application)
                    .user(application.getUser())
                    .executionMode("SANDBOX")
                    .status("FAILED")
                    .submissionSimulated(false)
                    .submissionVerified(false)
                    .realSubmissionAttempted(false)
                    .startedAt(startTime)
                    .completedAt(LocalDateTime.now())
                    .errorCode(e.getMessage())
                    .build();

            sandboxRunRepository.save(run);

            return SandboxExecutionResultDto.builder()
                    .applicationId(applicationId)
                    .executionMode("SANDBOX")
                    .status("FAILED")
                    .realSubmissionAttempted(false)
                    .emailSent(false)
                    .fileUploadedToRealProvider(false)
                    .errorCode(e.getMessage())
                    .build();

        } finally {
            try {
                distributedExecutionLock.release(lockKey, "LOCK_M6C_SANDBOX");
            } catch (Exception e) {
                log.warn("Error releasing lock {}", lockKey, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SandboxExecutionResultDto getLatestSandboxStatus(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own application ID: " + applicationId);
        }

        return sandboxRunRepository.findTopByApplicationIdAndUserIdOrderByCreatedAtDesc(applicationId, userId)
                .map(run -> SandboxExecutionResultDto.builder()
                        .applicationId(run.getApplication().getId())
                        .executionMode(run.getExecutionMode())
                        .status(run.getStatus())
                        .fieldsDetected(run.getFieldsDetected())
                        .fieldsMapped(run.getFieldsMapped())
                        .fieldsVerified(run.getFieldsVerified())
                        .fieldsRequireReview(run.getFieldsRequireReview())
                        .submissionSimulated(run.isSubmissionSimulated())
                        .submissionVerified(run.isSubmissionVerified())
                        .realSubmissionAttempted(false)
                        .emailSent(false)
                        .fileUploadedToRealProvider(false)
                        .startedAt(run.getStartedAt())
                        .completedAt(run.getCompletedAt())
                        .errorCode(run.getErrorCode())
                        .build())
                .orElseGet(() -> SandboxExecutionResultDto.builder()
                        .applicationId(applicationId)
                        .executionMode("SANDBOX")
                        .status("NOT_STARTED")
                        .realSubmissionAttempted(false)
                        .emailSent(false)
                        .fileUploadedToRealProvider(false)
                        .build());
    }

    private void recordAuditSafely(Long userId, Long applicationId, String action, String summary) {
        try {
            auditService.recordAudit(userId, applicationId, "PLAYWRIGHT_SANDBOX_EXECUTION", action, "SUCCESS", summary, "sandbox=true", 100L, null);
        } catch (Exception e) {
            log.warn("Failed to record audit log cleanly for sandbox execution", e);
        }
    }
}
