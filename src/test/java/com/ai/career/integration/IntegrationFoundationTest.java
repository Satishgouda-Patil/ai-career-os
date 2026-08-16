package com.ai.career.integration;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.domain.entity.IntegrationAuditLog;
import com.ai.career.integration.domain.entity.IntegrationCredential;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import com.ai.career.integration.domain.repository.IntegrationCredentialRepository;
import com.ai.career.integration.registry.ProductionProviderRegistry;
import com.ai.career.integration.service.CredentialEncryptionService;
import com.ai.career.integration.service.IntegrationAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationFoundationTest {

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private IntegrationAuditService auditService;

    @Autowired
    private IntegrationCredentialRepository credentialRepository;

    @Autowired
    private IntegrationAuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductionProviderRegistry providerRegistry;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        auditLogRepository.deleteAll();
        credentialRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(User.builder()
                .email("user1-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        user2 = userRepository.save(User.builder()
                .email("user2-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());
    }

    @Test
    void testAES256GCMEncryptionAndDecryption() {
        String secretPayload = "{\"apiKey\":\"sk_live_123456789\",\"secret\":\"supersecret\"}";
        CredentialEncryptionService.EncryptedData encrypted = encryptionService.encrypt(secretPayload);

        assertThat(encrypted.ciphertext()).isNotNull().isNotEqualTo(secretPayload);
        assertThat(encrypted.iv()).isNotNull();

        String decrypted = encryptionService.decrypt(encrypted.ciphertext(), encrypted.iv());
        assertThat(decrypted).isEqualTo(secretPayload);
    }

    @Test
    void testEncryptionFailsSafelyForNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> encryptionService.encrypt(null));
        assertThrows(IllegalArgumentException.class, () -> encryptionService.encrypt(""));
    }

    @Test
    @Transactional
    void testCredentialPersistenceAndTenantIsolation() {
        CredentialEncryptionService.EncryptedData enc = encryptionService.encrypt("secret-token");

        credentialRepository.save(IntegrationCredential.builder()
                .user(user1)
                .providerName("JOOBLE")
                .encryptedPayload(enc.ciphertext())
                .payloadIv(enc.iv())
                .status("ACTIVE")
                .build());

        List<IntegrationCredential> user1Creds = credentialRepository.findByUserId(user1.getId());
        List<IntegrationCredential> user2Creds = credentialRepository.findByUserId(user2.getId());

        assertThat(user1Creds).hasSize(1);
        assertThat(user1Creds.get(0).getProviderName()).isEqualTo("JOOBLE");
        assertThat(user2Creds).isEmpty();
    }

    @Test
    @Transactional
    void testSanitizedAuditLogPersistence() {
        String rawRequest = "POST /api api-key=secretKey123&password=myPassword";
        String rawResponse = "HTTP 200 token=bearerToken999";

        IntegrationAuditLog savedLog = auditService.recordAudit(
                user1.getId(),
                null,
                "JOOBLE",
                "FETCH_JOBS",
                "SUCCESS",
                rawRequest,
                rawResponse,
                150L,
                null
        );

        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getRequestSummary()).doesNotContain("secretKey123", "myPassword");
        assertThat(savedLog.getResponseSummary()).doesNotContain("bearerToken999");
        assertThat(savedLog.getRequestSummary()).contains("***SANITIZED***");
    }

    @Test
    void testProviderRegistry() {
        ProductionProviderRegistry.ProviderPlugin plugin = new ProductionProviderRegistry.ProviderPlugin() {
            @Override
            public String getProviderName() {
                return "SANDBOX_MOCK_PROVIDER";
            }

            @Override
            public ProductionProviderRegistry.ProviderCategory getCategory() {
                return ProductionProviderRegistry.ProviderCategory.JOB_DISCOVERY;
            }

            @Override
            public boolean isSandbox() {
                return true;
            }
        };

        providerRegistry.registerProvider(plugin);
        assertThat(providerRegistry.getProvider("SANDBOX_MOCK_PROVIDER")).isPresent();
        assertThat(providerRegistry.getProvider("SANDBOX_MOCK_PROVIDER").get().isSandbox()).isTrue();
    }
}
