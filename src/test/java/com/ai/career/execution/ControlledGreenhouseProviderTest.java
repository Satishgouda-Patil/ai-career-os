package com.ai.career.execution;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.provider.ExecutionContext;
import com.ai.career.execution.provider.ExecutionResult;
import com.ai.career.execution.provider.ExecutionValidationResult;
import com.ai.career.execution.provider.GreenhouseApplicationProviderImpl;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ControlledGreenhouseProviderTest {

    @Autowired
    private GreenhouseApplicationProviderImpl greenhouseProvider;

    @Autowired
    private ApplicationStateMachine stateMachine;

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
    private Job greenhouseJob;
    private Application app;

    @BeforeEach
    void setUp() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        auditLogRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("gh-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        long ts = System.currentTimeMillis();
        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-job-" + ts)
                .title("Senior Cloud Architect")
                .company("Acme Corp")
                .location("Remote")
                .description("Cloud Native Solutions")
                .url("https://boards.greenhouse.io/acmecorp/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testProviderSupportsGreenhouseUrls() {
        assertThat(greenhouseProvider.supports(app)).isTrue();

        Job nonGhJob = Job.builder().url("https://example.com/jobs/1").build();
        Application nonGhApp = Application.builder().job(nonGhJob).build();
        assertThat(greenhouseProvider.supports(nonGhApp)).isFalse();
    }

    @Test
    @Transactional
    void testThreeStepStateTransitionFlow() {
        // Step 1: READY_FOR_REVIEW -> APPROVED
        stateMachine.validateTransition(app.getStatus(), ApplicationState.APPROVED);
        app.setStatus(ApplicationState.APPROVED);

        // Step 2: APPROVED -> CONFIRMED_SUBMISSION
        stateMachine.validateTransition(app.getStatus(), ApplicationState.CONFIRMED_SUBMISSION);
        app.setStatus(ApplicationState.CONFIRMED_SUBMISSION);

        assertThat(app.getStatus()).isEqualTo(ApplicationState.CONFIRMED_SUBMISSION);
    }

    @Test
    void testRejectionOfExecutionWithoutConfirmedSubmissionState() {
        // App is still in READY_FOR_REVIEW
        ExecutionContext context = ExecutionContext.builder()
                .dryRun(true)
                .jobUrl(greenhouseJob.getUrl())
                .applicationAnswers(Map.of())
                .build();

        ExecutionValidationResult val = greenhouseProvider.validate(app, context);
        assertThat(val.isValid()).isFalse();
        assertThat(val.getValidationErrors()).anyMatch(err -> err.contains("Candidate explicit approval missing") || err.contains("CONFIRMED_SUBMISSION"));
    }

    @Test
    @Transactional
    void testDryRunExecutionWithConfirmedSubmissionState() {
        app.setStatus(ApplicationState.CONFIRMED_SUBMISSION);
        applicationRepository.save(app);

        ExecutionContext context = ExecutionContext.builder()
                .dryRun(true)
                .jobUrl(greenhouseJob.getUrl())
                .applicationAnswers(Map.of())
                .build();

        ExecutionResult result = greenhouseProvider.execute(app, context);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getExternalApplicationId()).contains("GREENHOUSE_DRY_RUN");

        assertThat(auditLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).isNotEmpty();
    }

    @Test
    void testLiveSubmissionBlockedWhenAllowLiveSubmissionIsFalse() {
        app.setStatus(ApplicationState.CONFIRMED_SUBMISSION);
        applicationRepository.save(app);

        ExecutionContext context = ExecutionContext.builder()
                .dryRun(false) // Requesting LIVE submission
                .jobUrl(greenhouseJob.getUrl())
                .applicationAnswers(Map.of())
                .build();

        ExecutionValidationResult val = greenhouseProvider.validate(app, context);
        assertThat(val.isValid()).isFalse();
        assertThat(val.getValidationErrors()).anyMatch(err -> err.contains("Live submission configuration is DISABLED"));
    }
}
