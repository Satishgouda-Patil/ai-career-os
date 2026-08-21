package com.ai.career.execution.sandbox;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;
import com.ai.career.execution.sandbox.provider.SandboxApplicationExecutionProvider;
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
class SandboxApplicationExecutionProviderTest {

    @Autowired
    private SandboxApplicationExecutionProvider sandboxProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

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

        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("sandbox-provider-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        long ts = System.currentTimeMillis();
        greenhouseJob = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("gh-sandbox-provider-" + ts)
                .title("Lead Infrastructure Engineer")
                .company("Acme Infra")
                .location("Remote")
                .description("Kubernetes & Java Microservices")
                .url("https://boards.greenhouse.io/acmeinfra/jobs/" + ts)
                .build());

        app = applicationRepository.save(Application.builder()
                .user(user)
                .job(greenhouseJob)
                .status(ApplicationState.READY_FOR_REVIEW)
                .build());
    }

    @Test
    void testSandboxProviderExecutionOnlyNoExternalConnection() {
        SandboxExecutionResultDto result = sandboxProvider.executeSandboxApplication(app.getId(), user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getExecutionMode()).isEqualTo("SANDBOX");
        assertThat(result.isRealSubmissionAttempted()).isFalse();
        assertThat(result.isEmailSent()).isFalse();
        assertThat(result.isFileUploadedToRealProvider()).isFalse();
    }
}
