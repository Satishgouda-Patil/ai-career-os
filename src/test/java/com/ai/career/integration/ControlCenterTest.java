package com.ai.career.integration;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import com.ai.career.integration.dto.ControlCenterSummaryDto;
import com.ai.career.integration.service.ControlCenterService;
import com.ai.career.integration.service.IntegrationAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ControlCenterTest {

    @Autowired
    private ControlCenterService controlCenterService;

    @Autowired
    private IntegrationAuditService auditService;

    @Autowired
    private IntegrationAuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    private User user;

    @BeforeEach
    void setUp() {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .id(1L)
                .email("cc-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());
    }

    @Test
    @Transactional
    void testControlCenterSummaryReportingAndSafetyFlags() {
        auditService.recordAudit(
                user.getId(),
                null,
                "JOOBLE_PRODUCTION",
                "FETCH_JOBS",
                "SUCCESS",
                "keywords=java",
                "fetched=5",
                120L,
                null
        );

        ControlCenterSummaryDto summary = controlCenterService.getSummary(user.getId());

        assertThat(summary).isNotNull();
        assertThat(summary.getOperatingMode()).contains("READ-ONLY");

        // Safety Flags Verification
        assertThat(summary.getSafetyFlags().isAutoApply()).isFalse();
        assertThat(summary.getSafetyFlags().isAutoSendEmail()).isFalse();
        assertThat(summary.getSafetyFlags().isAutoLinkedIn()).isFalse();
        assertThat(summary.getSafetyFlags().isAllowLiveSubmission()).isFalse();

        // Providers Verification
        assertThat(summary.getProviders()).hasSize(3);
        assertThat(summary.getProviders()).extracting("providerName")
                .contains("JOOBLE_PRODUCTION", "IMAP_PRODUCTION_READONLY", "GREENHOUSE_PRODUCTION");

        // Audits Verification
        assertThat(summary.getRecentAudits()).hasSize(1);
        assertThat(summary.getRecentAudits().get(0).getProviderName()).isEqualTo("JOOBLE_PRODUCTION");
    }

    @Test
    void testTenantIsolationForAuditLogRetrieval() {
        User user2 = userRepository.save(User.builder()
                .email("cc-user2-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        auditService.recordAudit(
                user.getId(),
                null,
                "IMAP_PRODUCTION_READONLY",
                "FETCH_EMAILS",
                "SUCCESS",
                "action=READONLY",
                "fetched=2",
                100L,
                null
        );

        ControlCenterSummaryDto user2Summary = controlCenterService.getSummary(user2.getId());
        assertThat(user2Summary.getRecentAudits()).isEmpty();
    }
}
