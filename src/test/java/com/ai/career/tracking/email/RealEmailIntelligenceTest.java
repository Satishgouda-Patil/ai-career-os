package com.ai.career.tracking.email;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.domain.entity.IntegrationCredential;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import com.ai.career.integration.domain.repository.IntegrationCredentialRepository;
import com.ai.career.integration.service.CredentialEncryptionService;
import com.ai.career.tracking.email.dto.RawEmailMessageDto;
import com.ai.career.tracking.email.provider.RealImapEmailProviderImpl;
import com.ai.career.tracking.email.service.EmailClassifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class RealEmailIntelligenceTest {

    @Autowired
    private RealImapEmailProviderImpl realImapEmailProvider;

    @Autowired
    private EmailClassifierService emailClassifierService;

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
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .id(1L)
                .email("imap-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());
    }

    @Test
    void testEncryptedIMAPCredentialStorage() {
        CredentialEncryptionService.EncryptedData enc = encryptionService.encrypt("imap-oauth-token-999");

        credentialRepository.save(IntegrationCredential.builder()
                .user(user)
                .providerName("IMAP_PRODUCTION_READONLY")
                .encryptedPayload(enc.ciphertext())
                .payloadIv(enc.iv())
                .status("ACTIVE")
                .build());

        IntegrationCredential fetched = credentialRepository.findByUserIdAndProviderName(user.getId(), "IMAP_PRODUCTION_READONLY").orElseThrow();
        String decryptedToken = encryptionService.decrypt(fetched.getEncryptedPayload(), fetched.getPayloadIv());

        assertThat(decryptedToken).isEqualTo("imap-oauth-token-999");
    }

    @Test
    void testIMAPRateLimitingEnforcement() {
        RealImapEmailProviderImpl provider = new RealImapEmailProviderImpl(
                encryptionService,
                credentialRepository,
                null,
                new com.ai.career.integration.registry.ProductionProviderRegistry()
        );

        for (int i = 0; i < 2; i++) {
            provider.checkRateLimit();
        }

        assertThrows(IllegalStateException.class, provider::checkRateLimit);
    }

    @Test
    void testNoOutboundSMTPMethodsExistOnProvider() {
        Method[] methods = RealImapEmailProviderImpl.class.getDeclaredMethods();
        for (Method m : methods) {
            assertThat(m.getName().toLowerCase()).doesNotContain("send", "smtp", "outbound", "post", "write");
        }
    }

    @Test
    void testEmailClassificationIntegration() {
        RawEmailMessageDto message = RawEmailMessageDto.builder()
                .provider("IMAP_PRODUCTION_READONLY")
                .externalMessageId("m-1")
                .externalThreadId("t-1")
                .sender("recruiter@apexsystems.com")
                .subject("Invitation to Interview for Cloud Architect")
                .bodySnippet("We invite you to interview next Tuesday.")
                .receivedAt(LocalDateTime.now())
                .build();

        com.ai.career.tracking.email.dto.EmailClassificationDto classification = emailClassifierService.classify(
                message.getSender(),
                message.getSubject(),
                message.getBodySnippet()
        );

        assertThat(classification).isNotNull();
        assertThat(classification.getClassification()).isNotNull();
    }
}
