package com.ai.career.job;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.domain.entity.IntegrationAuditLog;
import com.ai.career.integration.domain.entity.IntegrationCredential;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import com.ai.career.integration.domain.repository.IntegrationCredentialRepository;
import com.ai.career.integration.service.CredentialEncryptionService;
import com.ai.career.job.connector.ProductionJoobleJobFetcher;
import com.ai.career.job.dto.JobDto;
import com.ai.career.job.service.JobIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class RealJobDiscoveryProviderTest {

    @Autowired
    private ProductionJoobleJobFetcher productionJoobleJobFetcher;

    @Autowired
    private JobIngestionService jobIngestionService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IntegrationCredentialRepository credentialRepository;

    @Autowired
    private IntegrationAuditLogRepository auditLogRepository;

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    private User user;

    @BeforeEach
    void setUp() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        auditLogRepository.deleteAll();
        credentialRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .id(1L)
                .email("prod-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());
    }

    @Test
    void testEncryptedCredentialStoreAndRetrieval() {
        CredentialEncryptionService.EncryptedData enc = encryptionService.encrypt("jooble-live-key-xyz");

        credentialRepository.save(IntegrationCredential.builder()
                .user(user)
                .providerName("JOOBLE_PRODUCTION")
                .encryptedPayload(enc.ciphertext())
                .payloadIv(enc.iv())
                .status("ACTIVE")
                .build());

        IntegrationCredential fetched = credentialRepository.findByUserIdAndProviderName(user.getId(), "JOOBLE_PRODUCTION").orElseThrow();
        String decryptedKey = encryptionService.decrypt(fetched.getEncryptedPayload(), fetched.getPayloadIv());

        assertThat(decryptedKey).isEqualTo("jooble-live-key-xyz");
    }

    @Test
    void testRateLimitingEnforcement() {
        ProductionJoobleJobFetcher fetcher = new ProductionJoobleJobFetcher(
                org.springframework.web.client.RestClient.builder(),
                encryptionService,
                credentialRepository,
                null,
                new com.ai.career.integration.registry.ProductionProviderRegistry(),
                "https://jooble.org/api"
        );

        for (int i = 0; i < 10; i++) {
            fetcher.checkRateLimit();
        }

        assertThrows(IllegalStateException.class, fetcher::checkRateLimit);
    }

    @Test
    @Transactional
    void testJobIngestionDeduplication() {
        JobDto job1 = JobDto.builder()
                .source("JOOBLE_PRODUCTION")
                .sourceJobId("PROD_JOB_101")
                .title("Lead Cloud Architect")
                .company("Apex Global")
                .location("Remote")
                .description("Cloud Native Solutions")
                .url("https://example.com/job101")
                .postedAt(LocalDateTime.now())
                .build();

        JobDto duplicateJob = JobDto.builder()
                .source("JOOBLE_PRODUCTION")
                .sourceJobId("PROD_JOB_101")
                .title("Lead Cloud Architect")
                .company("Apex Global")
                .location("Remote")
                .description("Cloud Native Solutions")
                .url("https://example.com/job101")
                .postedAt(LocalDateTime.now())
                .build();

        int firstIngest = jobIngestionService.ingestJobs(List.of(job1));
        int duplicateIngest = jobIngestionService.ingestJobs(List.of(duplicateJob));

        assertThat(firstIngest).isEqualTo(1);
        assertThat(duplicateIngest).isEqualTo(0);
        assertThat(jobRepository.findAll()).hasSize(1);
    }
}
