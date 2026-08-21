package com.ai.career.execution.sandbox;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.sandbox.domain.repository.SandboxExecutionRunRepository;
import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import com.ai.career.execution.sandbox.service.SandboxExecutionService;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class SandboxExecutionServiceTest {

    @Autowired
    private SandboxExecutionService sandboxExecutionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private SandboxExecutionRunRepository sandboxRunRepository;

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
        sandboxRunRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("sandbox-svc-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        long ts = System.currentTimeMillis();
        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-sandbox-svc-" + ts)
                .title("Senior Cloud Architect")
                .company("Acme Cloud")
                .location("Remote")
                .description("Spring Boot & AWS Cloud Infrastructure")
                .url("https://boards.greenhouse.io/acmecloud/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testSuccessfulSandboxExecutionWorkflow() {
        SandboxExecutionResultDto result = sandboxExecutionService.executeSandbox(app.getId(), user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getExecutionMode()).isEqualTo("SANDBOX");
        assertThat(result.getStatus()).isEqualTo("VERIFIED");
        assertThat(result.isSubmissionSimulated()).isTrue();
        assertThat(result.isSubmissionVerified()).isTrue();
        assertThat(result.isRealSubmissionAttempted()).isFalse();
        assertThat(result.isEmailSent()).isFalse();

        // Verify entity persisted in database
        assertThat(sandboxRunRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).isNotEmpty();
    }

    @Test
    void testSandboxExecutionFailsWhenLockNotAcquired() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(false);

        SandboxExecutionResultDto result = sandboxExecutionService.executeSandbox(app.getId(), user.getId());

        assertThat(result.getStatus()).isEqualTo("LOCK_NOT_ACQUIRED");
        assertThat(result.isRealSubmissionAttempted()).isFalse();
        assertThat(result.getExecutionMode()).isEqualTo("SANDBOX");
    }

    @Test
    void testGetLatestSandboxStatus() {
        sandboxExecutionService.executeSandbox(app.getId(), user.getId());

        SandboxExecutionResultDto status = sandboxExecutionService.getLatestSandboxStatus(app.getId(), user.getId());

        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo("VERIFIED");
        assertThat(status.isRealSubmissionAttempted()).isFalse();
    }
}
