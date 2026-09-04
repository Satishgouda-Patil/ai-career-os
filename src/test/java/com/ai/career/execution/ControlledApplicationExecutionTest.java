package com.ai.career.execution;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.gate.LiveSubmissionSafetyGate;
import com.ai.career.execution.service.ControlledApplicationExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ControlledApplicationExecutionTest {

    @Autowired
    private ControlledApplicationExecutionService controlledExecutionService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LiveSubmissionSafetyGate safetyGate;

    @Autowired
    private com.ai.career.domain.repository.JobRepository jobRepository;

    private User testUser;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("m6f-candidate-" + System.currentTimeMillis() + "@ai-career.os")
                .passwordHash("hashed")
                .build());

        Job testJob = jobRepository.save(Job.builder()
                .source("GREENHOUSE")
                .sourceJobId("gh-m6f-99")
                .title("Lead End-to-End Test Architect")
                .company("Safety First Corp")
                .location("Remote")
                .description("Build controlled human-gated browser automation systems.")
                .url("https://boards.greenhouse.io/safetyfirst/jobs/99")
                .build());

        testApplication = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .providerName("GREENHOUSE_PRODUCTION")
                .build());
    }

    @Test
    @DisplayName("M6-F 1: Full 12-Step Controlled Execution Workflow produces BLOCKED status when live submission is disabled")
    void testFullControlledWorkflowBlocked() {
        // Step 1: Start
        ControlledExecutionStepResponse startRes = controlledExecutionService.startExecution(
                testUser, testApplication.getId(), ControlledExecutionStartRequest.builder().mode("PRODUCTION_READ_ONLY").build()
        );
        assertNotNull(startRes);
        assertEquals("INSPECTING", startRes.getWorkflowStatus());

        // Step 2 & 3: Inspect
        ControlledExecutionStepResponse inspectRes = controlledExecutionService.inspectApplication(testUser, testApplication.getId());
        assertEquals("INSPECTING", inspectRes.getWorkflowStatus());

        // Step 4: Map
        ControlledExecutionStepResponse mapRes = controlledExecutionService.mapCandidateFields(testUser, testApplication.getId());
        assertEquals("MAPPING", mapRes.getWorkflowStatus());

        // Step 5: Sandbox
        ControlledExecutionStepResponse sandboxRes = controlledExecutionService.runSandboxVerification(testUser, testApplication.getId());
        assertEquals("SANDBOX_VERIFYING", sandboxRes.getWorkflowStatus());

        // Step 6: Prepare
        ControlledExecutionStepResponse prepRes = controlledExecutionService.prepareProductionExecution(testUser, testApplication.getId());
        assertEquals("AWAITING_REVIEW", prepRes.getWorkflowStatus());
        assertNotNull(prepRes.getPreviewId());
        assertNotNull(prepRes.getFormFingerprint());

        // Step 7: Safety Review
        ControlledExecutionSafetyReviewResponse reviewRes = controlledExecutionService.getSafetyReview(testUser, testApplication.getId());
        assertNotNull(reviewRes);
        assertFalse(reviewRes.isAllowLiveSubmission());
        assertTrue(reviewRes.isReadyForConfirmation());

        // Step 8: Candidate Confirmation
        ControlledExecutionStepResponse confirmRes = controlledExecutionService.confirmCandidate(
                testUser, testApplication.getId(), ControlledExecutionConfirmRequest.builder()
                        .previewId(prepRes.getPreviewId())
                        .formFingerprint(prepRes.getFormFingerprint())
                        .acknowledged(true)
                        .build()
        );
        assertEquals("AWAITING_CONFIRMATION", confirmRes.getWorkflowStatus());
        assertNotNull(confirmRes.getConfirmationId());

        // Step 9 & 10: Execution
        ControlledExecutionStepResponse executeRes = controlledExecutionService.execute(
                testUser, testApplication.getId(), ControlledExecutionRunRequest.builder()
                        .mode("PRODUCTION_READ_ONLY")
                        .previewId(prepRes.getPreviewId())
                        .confirmationId(confirmRes.getConfirmationId())
                        .formFingerprint(prepRes.getFormFingerprint())
                        .build()
        );

        assertNotNull(executeRes);
        assertEquals("BLOCKED", executeRes.getWorkflowStatus());
        assertFalse(executeRes.isSubmissionAttempted());
        assertFalse(executeRes.isEmailSent());
        assertFalse(executeRes.isFileUploaded());
        assertEquals("LIVE_SUBMISSION_DISABLED", executeRes.getFailureCode());

        // Step 11 & 12: Verification
        ControlledExecutionStepResponse verifyRes = controlledExecutionService.verify(testUser, testApplication.getId());
        assertNotNull(verifyRes);

        // Status check
        ControlledExecutionStatusResponse status = controlledExecutionService.getStatus(testUser, testApplication.getId());
        assertEquals("BLOCKED", status.getWorkflowStatus());
        assertFalse(status.isSubmissionAttempted());
    }

    @Test
    @DisplayName("M6-F 2: Execution without candidate confirmation is rejected with CANDIDATE_CONFIRMATION_REQUIRED")
    void testExecutionWithoutConfirmationFails() {
        controlledExecutionService.startExecution(testUser, testApplication.getId(), ControlledExecutionStartRequest.builder().mode("PRODUCTION_READ_ONLY").build());
        controlledExecutionService.prepareProductionExecution(testUser, testApplication.getId());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            controlledExecutionService.execute(testUser, testApplication.getId(), ControlledExecutionRunRequest.builder()
                    .mode("PRODUCTION_READ_ONLY")
                    .build());
        });

        assertTrue(exception.getMessage().contains("CANDIDATE_CONFIRMATION_REQUIRED"));
    }

    @Test
    @DisplayName("M6-F 3: Execution with stale fingerprint is rejected with FORM_FINGERPRINT_CHANGED")
    void testExecutionWithStaleFingerprintFails() {
        controlledExecutionService.startExecution(testUser, testApplication.getId(), ControlledExecutionStartRequest.builder().mode("PRODUCTION_READ_ONLY").build());
        ControlledExecutionStepResponse prepRes = controlledExecutionService.prepareProductionExecution(testUser, testApplication.getId());
        
        controlledExecutionService.confirmCandidate(testUser, testApplication.getId(), ControlledExecutionConfirmRequest.builder()
                .previewId(prepRes.getPreviewId())
                .formFingerprint(prepRes.getFormFingerprint())
                .acknowledged(true)
                .build());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            controlledExecutionService.execute(testUser, testApplication.getId(), ControlledExecutionRunRequest.builder()
                    .mode("PRODUCTION_READ_ONLY")
                    .formFingerprint("stale-fingerprint-999")
                    .build());
        });

        assertTrue(exception.getMessage().contains("FORM_FINGERPRINT_CHANGED"));
    }

    @Test
    @DisplayName("M6-F 4: Server configuration invariants enforce ALLOW_LIVE_SUBMISSION=false")
    void testServerSafetyInvariants() {
        assertFalse(safetyGate.isLiveSubmissionAllowed());
    }
}
