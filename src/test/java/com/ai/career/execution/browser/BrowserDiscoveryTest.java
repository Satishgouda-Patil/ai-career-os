package com.ai.career.execution.browser;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.browser.dto.BrowserDiscoveryResultDto;
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
class BrowserDiscoveryTest {

    @Autowired
    private BrowserSafetyPolicy safetyPolicy;

    @Autowired
    private BrowserDiscoveryService discoveryService;

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
                .email("browser-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        long ts = System.currentTimeMillis();
        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-browser-" + ts)
                .title("Lead Full Stack Engineer")
                .company("Acme Systems")
                .location("Remote")
                .description("React & Spring Boot Solutions")
                .url("https://boards.greenhouse.io/acmesystems/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testUrlSafetyPolicyValidation() {
        assertThat(safetyPolicy.isUrlSafe("https://boards.greenhouse.io/acme/jobs/123")).isTrue();
        assertThat(safetyPolicy.isUrlSafe("http://localhost:5173/")).isTrue();

        // Unsupported / malicious domain rejection
        assertThat(safetyPolicy.isUrlSafe("https://malicious-phishing.com/apply")).isFalse();
        assertThat(safetyPolicy.isUrlSafe("ftp://invalid-protocol.com")).isFalse();
        assertThat(safetyPolicy.isUrlSafe(null)).isFalse();
    }

    @Test
    void testDiscoveryRejectsUnsupportedDomainSafely() {
        long ts = System.currentTimeMillis();
        Job unsafeJob = jobRepository.save(Job.builder()
                .source("UNSAFE")
                .sourceJobId("unsafe-" + ts)
                .title("Software Engineer")
                .company("External Corp")
                .description("Test Description")
                .url("https://unsupported-external-portal.com/job/1")
                .build());

        Application unsafeApp = applicationRepository.save(Application.builder()
                .user(user)
                .job(unsafeJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());

        BrowserDiscoveryResultDto result = discoveryService.discoverForm(unsafeApp.getId(), user.getId());

        assertThat(result.getStatus()).isEqualTo("DOMAIN_NOT_ALLOWED");
        assertThat(result.isSubmissionAttempted()).isFalse();
        assertThat(result.getFields()).isEmpty();
    }

    @Test
    void testDiscoveryFailsWhenLockNotAcquired() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(false);

        BrowserDiscoveryResultDto result = discoveryService.discoverForm(app.getId(), user.getId());

        assertThat(result.getStatus()).isEqualTo("LOCK_NOT_ACQUIRED");
        assertThat(result.isSubmissionAttempted()).isFalse();
        assertThat(result.getExecutionMode()).isEqualTo("READ_ONLY");
    }

    @Test
    void testDiscoveryAuditRecording() {
        BrowserDiscoveryResultDto result = discoveryService.discoverForm(app.getId(), user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getExecutionMode()).isEqualTo("READ_ONLY");
        assertThat(result.isSubmissionAttempted()).isFalse();

        assertThat(auditLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).isNotEmpty();
    }
}
