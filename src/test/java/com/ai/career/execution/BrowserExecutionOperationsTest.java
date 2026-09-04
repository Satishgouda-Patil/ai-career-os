package com.ai.career.execution;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.domain.entity.BrowserExecutionRun;
import com.ai.career.execution.domain.repository.BrowserExecutionRunRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.model.BrowserExecutionMode;
import com.ai.career.execution.model.BrowserExecutionStatus;
import com.ai.career.execution.service.BrowserExecutionOperationsService;
import com.ai.career.execution.service.ExecutionSafetyConfigurationService;
import com.ai.career.execution.service.ProviderHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BrowserExecutionOperationsTest {

    @Autowired
    private BrowserExecutionOperationsService operationsService;

    @Autowired
    private ExecutionSafetyConfigurationService safetyConfigService;

    @Autowired
    private ProviderHealthService providerHealthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private BrowserExecutionRunRepository runRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;
    private User otherUser;
    private Application app;
    private BrowserExecutionRun run;

    @BeforeEach
    void setUp() {
        runRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        long ts = System.currentTimeMillis();

        user = userRepository.save(User.builder()
                .email("ops-user-" + ts + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        otherUser = userRepository.save(User.builder()
                .email("ops-other-" + ts + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        Job job = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("ops-job-" + ts)
                .title("Staff Cloud Engineer")
                .company("Acme Inc")
                .location("Remote")
                .description("Cloud Architecture")
                .url("https://boards.greenhouse.io/acmeinc/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status(ApplicationState.CONFIRMED_SUBMISSION)
                .build());

        run = runRepository.save(BrowserExecutionRun.builder()
                .user(user)
                .application(app)
                .providerName("GREENHOUSE_PRODUCTION")
                .executionMode(BrowserExecutionMode.PRODUCTION_READ_ONLY)
                .status(BrowserExecutionStatus.BLOCKED)
                .previewId("prev-ops-1")
                .formFingerprint("fp-ops-1")
                .submissionAttempted(false)
                .submissionVerified(false)
                .errorCode("LIVE_SUBMISSION_DISABLED")
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now().minusMinutes(5))
                .build());
    }

    @Test
    void testGetExecutionRuns() {
        List<ExecutionOperationsRunDto> runs = operationsService.getExecutionRuns(user, app.getId(), null, null);
        assertThat(runs).isNotEmpty();
        assertThat(runs.get(0).getProvider()).isEqualTo("GREENHOUSE_PRODUCTION");
        assertThat(runs.get(0).getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    void testGetExecutionRunDetail() {
        ExecutionOperationsRunDto detail = operationsService.getExecutionRunDetail(user, run.getId());
        assertThat(detail).isNotNull();
        assertThat(detail.getRunId()).isEqualTo(run.getId());
        assertThat(detail.getMode()).isEqualTo("PRODUCTION_READ_ONLY");
    }

    @Test
    void testCrossUserAccessBlocked() {
        assertThatThrownBy(() -> operationsService.getExecutionRunDetail(otherUser, run.getId()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void testRetryEligibilityWhenLiveDisabled() {
        RetryEligibilityResponse eligibility = operationsService.getRetryEligibility(user, run.getId());
        assertThat(eligibility.isEligible()).isFalse();
        assertThat(eligibility.getReason()).contains("LIVE_SUBMISSION_DISABLED");
    }

    @Test
    void testRetryExecutionBlockedWhenLiveDisabled() {
        ExecutionOperationsRunDto result = operationsService.retryExecution(user, run.getId());
        assertThat(result.getStatus()).isEqualTo("BLOCKED");
        assertThat(result.isSubmissionAttempted()).isFalse();
        assertThat(result.getFailureReason()).contains("Live submission is disabled");
    }

    @Test
    void testGetEffectiveSafetyConfiguration() {
        SafetyConfigurationResponse config = safetyConfigService.getEffectiveSafetyConfiguration();
        assertThat(config.isAllowLiveSubmission()).isFalse();
        assertThat(config.isAutoApply()).isFalse();
        assertThat(config.isAutoSendEmail()).isFalse();
        assertThat(config.isAutoLinkedIn()).isFalse();
    }

    @Test
    void testGetProviderHealth() {
        ProviderHealthResponse health = providerHealthService.getProviderHealth();
        assertThat(health.getProviders()).isNotEmpty();
        assertThat(health.getProviders()).anyMatch(p -> "GREENHOUSE_PRODUCTION".equals(p.getProvider()) && p.isReachable());
    }

    @Test
    void testGetExecutionMetrics() {
        ExecutionMetricsResponse metrics = operationsService.getExecutionMetrics(user);
        assertThat(metrics.getTotalExecutions()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.getRealSubmissions()).isEqualTo(0);
        assertThat(metrics.getEmailsSent()).isEqualTo(0);
    }

    @Test
    @Transactional
    void testRecoverStaleExecutions() {
        BrowserExecutionRun staleRun = runRepository.save(BrowserExecutionRun.builder()
                .user(user)
                .application(app)
                .providerName("GREENHOUSE_PRODUCTION")
                .executionMode(BrowserExecutionMode.PRODUCTION_READ_ONLY)
                .status(BrowserExecutionStatus.APPLYING)
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .build());

        int recovered = operationsService.recoverStaleExecutions();
        assertThat(recovered).isGreaterThanOrEqualTo(1);

        BrowserExecutionRun updated = runRepository.findById(staleRun.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(BrowserExecutionStatus.FAILED);
        assertThat(updated.getErrorCode()).isEqualTo("EXECUTION_TIMEOUT_OR_STALE_SESSION");
    }
}
