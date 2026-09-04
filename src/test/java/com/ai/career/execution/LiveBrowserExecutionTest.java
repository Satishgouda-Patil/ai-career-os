package com.ai.career.execution;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.gate.LiveSubmissionSafetyGate;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.service.LiveBrowserExecutionService;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class LiveBrowserExecutionTest {

    @Autowired
    private LiveBrowserExecutionService liveExecutionService;

    @Autowired
    private LiveSubmissionSafetyGate safetyGate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private IntegrationAuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    private User user;
    private User otherUser;
    private Job greenhouseJob;
    private Job nonGreenhouseJob;
    private Application greenhouseApp;

    @BeforeEach
    void setUp() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        auditLogRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        long ts = System.currentTimeMillis();

        user = userRepository.save(User.builder()
                .email("live-user-" + ts + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        otherUser = userRepository.save(User.builder()
                .email("other-user-" + ts + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-job-" + ts)
                .title("Senior Systems Engineer")
                .company("CloudCorp")
                .location("Remote")
                .description("High scale distributed systems")
                .url("https://boards.greenhouse.io/cloudcorp/jobs/" + ts)
                .build());

        nonGreenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("other-job-" + ts)
                .title("Software Engineer")
                .company("OtherCorp")
                .location("Remote")
                .description("Regular tech stack")
                .url("https://example.com/jobs/" + ts)
                .build());

        greenhouseApp = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testPrepareLiveExecutionSuccess() {
        LiveExecutionPrepareResponse response = liveExecutionService.prepareLiveExecution(user, greenhouseApp.getId());

        assertThat(response).isNotNull();
        assertThat(response.getApplicationId()).isEqualTo(greenhouseApp.getId());
        assertThat(response.getMode()).isEqualTo("PRODUCTION_READ_ONLY");
        assertThat(response.getProvider()).isEqualTo("GREENHOUSE_PRODUCTION");
        assertThat(response.isSubmissionAttempted()).isFalse();
        assertThat(response.getFormFingerprint()).startsWith("fp-gh-");
    }

    @Test
    void testPrepareRejectsNonGreenhouseUrl() {
        Application nonGhApp = applicationRepository.save(Application.builder()
                .user(user)
                .job(nonGreenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());

        assertThatThrownBy(() -> liveExecutionService.prepareLiveExecution(user, nonGhApp.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a supported Greenhouse target");
    }

    @Test
    void testThreeStepApprovalFlowAndConfirm() {
        // Prepare
        LiveExecutionPrepareResponse prep = liveExecutionService.prepareLiveExecution(user, greenhouseApp.getId());

        // Confirm
        LiveExecutionConfirmResponse confirm = liveExecutionService.confirmLiveExecution(user, greenhouseApp.getId(), prep.getFormFingerprint());

        assertThat(confirm.getStatus()).isEqualTo("CONFIRMED_SUBMISSION");

        Application updated = applicationRepository.findById(greenhouseApp.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ApplicationState.CONFIRMED_SUBMISSION);
    }

    @Test
    void testLiveExecutionBlockedWhenAllowLiveSubmissionIsFalse() {
        // Step 1: Prepare
        LiveExecutionPrepareResponse prep = liveExecutionService.prepareLiveExecution(user, greenhouseApp.getId());

        // Step 2: Confirm
        liveExecutionService.confirmLiveExecution(user, greenhouseApp.getId(), prep.getFormFingerprint());

        // Step 3: Execute (ALLOW_LIVE_SUBMISSION is false by default)
        LiveExecutionExecuteResponse exec = liveExecutionService.executeLiveSubmission(user, greenhouseApp.getId(), prep.getFormFingerprint());

        assertThat(exec.getStatus()).isEqualTo("BLOCKED");
        assertThat(exec.isSubmissionAttempted()).isFalse();
        assertThat(exec.getErrorCode()).isEqualTo("LIVE_SUBMISSION_DISABLED");
        assertThat(exec.getErrorMessage()).contains("Live submission is DISABLED");
    }

    @Test
    void testUnauthenticatedOrCrossUserAccessBlocked() {
        assertThatThrownBy(() -> liveExecutionService.prepareLiveExecution(otherUser, greenhouseApp.getId()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void testGetLiveExecutionStatus() {
        liveExecutionService.prepareLiveExecution(user, greenhouseApp.getId());
        LiveExecutionStatusResponse status = liveExecutionService.getLiveExecutionStatus(user, greenhouseApp.getId());

        assertThat(status.getProvider()).isEqualTo("GREENHOUSE_PRODUCTION");
        assertThat(status.isAllowLiveSubmission()).isFalse();
        assertThat(status.isSubmissionAttempted()).isFalse();
    }
}
