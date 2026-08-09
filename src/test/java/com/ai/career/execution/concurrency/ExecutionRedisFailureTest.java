package com.ai.career.execution.concurrency;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.service.ApplicationExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Import(ExecutionRedisFailureTest.FailingRedisConfig.class)
public class ExecutionRedisFailureTest {

    @TestConfiguration
    static class FailingRedisConfig {
        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public StringRedisTemplate stringRedisTemplate() {
            StringRedisTemplate template = mock(StringRedisTemplate.class);
            ValueOperations<String, String> valueOps = mock(ValueOperations.class);
            when(template.opsForValue()).thenReturn(valueOps);

            // Simulate Redis Connection Failure
            when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RedisConnectionFailureException("Simulated Redis outage"));

            return template;
        }
    }

    @Autowired
    private ApplicationExecutionService executionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    public void testRedisFailureFailsClosedWithoutInvokingProvider() {
        User user = userRepository.save(User.builder()
            .email("redis.failure." + System.currentTimeMillis() + "@example.com")
            .passwordHash("hashedPassword123")
            .build());

        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M3-JOB-FAIL")
            .title("DevOps Engineer")
            .company("Cloud OS")
            .location("Remote")
            .description("DevOps")
            .url("https://example.com/fail")
            .build());

        Application application = applicationRepository.save(Application.builder()
            .user(user)
            .job(job)
            .status(ApplicationState.APPROVED)
            .providerName("MOCK")
            .build());

        // Execution must fail closed when Redis is unavailable
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            executionService.executeApplication(user.getId(), application.getId(), ExecuteApplicationRequest.builder().build());
        });

        assertTrue(ex.getMessage().contains("LOCK_NOT_ACQUIRED"), "Execution failure message must indicate failure to acquire execution lock when Redis is unavailable");

        // Verify Application State remains UNCHANGED from APPROVED (never transition to APPLIED or RUNNING)
        Application reloaded = applicationRepository.findById(application.getId()).orElseThrow();
        assertEquals(ApplicationState.APPROVED, reloaded.getStatus(), "Application state must remain unchanged when Redis lock acquisition fails");
    }
}
