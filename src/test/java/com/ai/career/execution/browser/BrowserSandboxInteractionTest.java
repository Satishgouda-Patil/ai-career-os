package com.ai.career.execution.browser;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.interaction.BrowserInteractionService;
import com.ai.career.browser.interaction.SubmissionPreview;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
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
class BrowserSandboxInteractionTest {

    @Autowired
    private BrowserInteractionService interactionService;

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
                .email("sandbox-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        long ts = System.currentTimeMillis();
        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-sandbox-" + ts)
                .title("Senior Backend Engineer")
                .company("Acme Corp")
                .location("Remote")
                .description("Java Spring Boot Developer Position")
                .url("https://boards.greenhouse.io/acmecorp/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testSandboxFormInteractionMetricsAndSafety() {
        SubmissionPreview preview = interactionService.executeInteraction(app.getId());

        assertThat(preview).isNotNull();
        assertThat(preview.getExecutionMode()).isEqualTo("SANDBOX");
        assertThat(preview.isSubmissionAttempted()).isFalse();
        assertThat(preview.isReadyForSubmission()).isFalse();
        assertThat(preview.getStatus()).isIn("READY_FOR_REVIEW", "CONTROLLED_INTERACTION_VERIFIED_STOPPED");
    }

    @Test
    void testSandboxInteractionFailsWhenLockNotAcquired() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(false);

        SubmissionPreview preview = interactionService.executeInteraction(app.getId());

        assertThat(preview.getStatus()).isEqualTo("LOCK_NOT_ACQUIRED");
        assertThat(preview.isSubmissionAttempted()).isFalse();
        assertThat(preview.isReadyForSubmission()).isFalse();
        assertThat(preview.getExecutionMode()).isEqualTo("SANDBOX");
    }

    @Test
    void testSandboxAuditRecording() {
        SubmissionPreview preview = interactionService.executeInteraction(app.getId());

        assertThat(preview).isNotNull();
        assertThat(auditLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).isNotEmpty();
    }
}
