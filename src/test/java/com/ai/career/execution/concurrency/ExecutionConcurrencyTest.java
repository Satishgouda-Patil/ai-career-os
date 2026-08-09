package com.ai.career.execution.concurrency;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.ExecutionResponse;
import com.ai.career.execution.service.ApplicationExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Import(ExecutionConcurrencyTest.TestRedisConfig.class)
public class ExecutionConcurrencyTest {

    @TestConfiguration
    static class TestRedisConfig {
        @Bean
        @Primary
        @SuppressWarnings("unchecked")
        public StringRedisTemplate stringRedisTemplate() {
            StringRedisTemplate template = mock(StringRedisTemplate.class);
            ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
            ValueOperations<String, String> valueOps = mock(ValueOperations.class);

            when(template.opsForValue()).thenReturn(valueOps);

            when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    String value = invocation.getArgument(1);
                    return store.putIfAbsent(key, value) == null;
                });

            when(template.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenAnswer(invocation -> {
                    List<String> keys = invocation.getArgument(1);
                    String owner = invocation.getArgument(2);
                    String key = keys.get(0);
                    if (owner.equals(store.get(key))) {
                        store.remove(key);
                        return 1L;
                    }
                    return 0L;
                });

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
    public void testConcurrentExecutionPreventsDuplicateProviderCalls() throws Exception {
        // 1. Setup User & Job
        User user = userRepository.save(User.builder()
            .email("concurrent.candidate." + System.currentTimeMillis() + "@example.com")
            .passwordHash("hashedPassword123")
            .build());

        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M3-JOB-" + System.currentTimeMillis())
            .title("Senior Cloud Architect")
            .company("Cloud OS Tech")
            .location("Remote")
            .description("Build cloud automation.")
            .url("https://example.com/p3-m3-job")
            .build());

        Application application = applicationRepository.save(Application.builder()
            .user(user)
            .job(job)
            .status(ApplicationState.APPROVED)
            .providerName("MOCK")
            .build());

        // 2. Launch 10 concurrent threads executing the same application ID
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> failureMessages = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to start simultaneously
                    ExecuteApplicationRequest req = ExecuteApplicationRequest.builder().dryRun(false).build();
                    ExecutionResponse resp = executionService.executeApplication(user.getId(), application.getId(), req);
                    if (resp != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                    failureMessages.add(ex.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Release threads concurrently
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrent threads execution timed out");

        // 3. Assert exactly ONE execution succeeded and remaining 9 failed with lock/idempotency exception
        assertEquals(1, successCount.get(), "Exactly one execution must succeed");
        assertEquals(threadCount - 1, failureCount.get(), "Remaining concurrent requests must fail closed");

        boolean lockedOrActiveMessage = failureMessages.stream()
            .anyMatch(msg -> msg.contains("LOCK_NOT_ACQUIRED") || msg.contains("active execution is already running"));
        assertTrue(lockedOrActiveMessage, "Failure message must indicate distributed lock acquisition or active execution safeguard");
    }

    @Test
    public void testDifferentApplicationsCanExecuteConcurrently() throws Exception {
        User user = userRepository.save(User.builder()
            .email("concurrent.diff." + System.currentTimeMillis() + "@example.com")
            .passwordHash("hashedPassword123")
            .build());

        Job job1 = jobRepository.save(Job.builder().source("JOOBLE").sourceJobId("P3-M3-JOB-DIFF-1").title("Dev 1").company("Co 1").location("Remote").description("Dev").url("https://example.com/1").build());
        Job job2 = jobRepository.save(Job.builder().source("JOOBLE").sourceJobId("P3-M3-JOB-DIFF-2").title("Dev 2").company("Co 2").location("Remote").description("Dev").url("https://example.com/2").build());

        Application app1 = applicationRepository.save(Application.builder().user(user).job(job1).status(ApplicationState.APPROVED).providerName("MOCK").build());
        Application app2 = applicationRepository.save(Application.builder().user(user).job(job2).status(ApplicationState.APPROVED).providerName("MOCK").build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<ExecutionResponse> f1 = executor.submit(() -> executionService.executeApplication(user.getId(), app1.getId(), ExecuteApplicationRequest.builder().build()));
        Future<ExecutionResponse> f2 = executor.submit(() -> executionService.executeApplication(user.getId(), app2.getId(), ExecuteApplicationRequest.builder().build()));

        ExecutionResponse r1 = f1.get(5, TimeUnit.SECONDS);
        ExecutionResponse r2 = f2.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(ApplicationState.APPLIED, r1.getApplicationStatus());
        assertEquals(ApplicationState.APPLIED, r2.getApplicationStatus());
    }
}
