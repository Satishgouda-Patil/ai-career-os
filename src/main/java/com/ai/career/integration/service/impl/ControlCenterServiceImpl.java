package com.ai.career.integration.service.impl;

import com.ai.career.integration.domain.entity.IntegrationAuditLog;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import com.ai.career.integration.domain.repository.IntegrationCredentialRepository;
import com.ai.career.integration.dto.ControlCenterSummaryDto;
import com.ai.career.integration.registry.ProductionProviderRegistry;
import com.ai.career.integration.service.ControlCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControlCenterServiceImpl implements ControlCenterService {

    private final IntegrationAuditLogRepository auditLogRepository;
    private final IntegrationCredentialRepository credentialRepository;
    private final ProductionProviderRegistry providerRegistry;

    @Value("${app.safety.auto-apply:false}")
    private boolean autoApply;

    @Value("${app.safety.auto-send-email:false}")
    private boolean autoSendEmail;

    @Value("${app.safety.auto-linkedin:false}")
    private boolean autoLinkedIn;

    @Value("${app.safety.allow-live-submission:false}")
    private boolean allowLiveSubmission;

    @Override
    public ControlCenterSummaryDto getSummary(Long userId) {
        String mode = allowLiveSubmission ? "PRODUCTION / LIVE EXECUTION" : "PRODUCTION / READ-ONLY";

        ControlCenterSummaryDto.SafetyFlagsDto flags = ControlCenterSummaryDto.SafetyFlagsDto.builder()
            .autoApply(autoApply)
            .autoSendEmail(autoSendEmail)
            .autoLinkedIn(autoLinkedIn)
            .allowLiveSubmission(allowLiveSubmission)
            .build();

        List<ControlCenterSummaryDto.ProviderHealthDto> providers = List.of(
            ControlCenterSummaryDto.ProviderHealthDto.builder()
                .providerName("JOOBLE_PRODUCTION")
                .category("JOB_DISCOVERY")
                .status("HEALTHY")
                .isSandbox(false)
                .rateLimitInfo("10 requests / min")
                .lastSync("Active")
                .build(),
            ControlCenterSummaryDto.ProviderHealthDto.builder()
                .providerName("IMAP_PRODUCTION_READONLY")
                .category("EMAIL_INTELLIGENCE")
                .status("HEALTHY")
                .isSandbox(false)
                .rateLimitInfo("2 requests / min (Read-Only)")
                .lastSync("Active")
                .build(),
            ControlCenterSummaryDto.ProviderHealthDto.builder()
                .providerName("GREENHOUSE_PRODUCTION")
                .category("APPLICATION_EXECUTION")
                .status("HEALTHY (DRY-RUN)")
                .isSandbox(false)
                .rateLimitInfo("Controlled Dry-Run Validation")
                .lastSync("Active")
                .build()
        );

        List<IntegrationAuditLog> userLogs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<ControlCenterSummaryDto.AuditLogSummaryDto> recentAudits = userLogs.stream()
            .limit(10)
            .map(this::mapAuditDto)
            .toList();

        List<ControlCenterSummaryDto.AuditLogSummaryDto> recentFailures = userLogs.stream()
            .filter(l -> "FAILED".equalsIgnoreCase(l.getStatus()))
            .limit(5)
            .map(this::mapAuditDto)
            .toList();

        return ControlCenterSummaryDto.builder()
            .operatingMode(mode)
            .safetyFlags(flags)
            .providers(providers)
            .recentAudits(recentAudits)
            .recentFailures(recentFailures)
            .build();
    }

    private ControlCenterSummaryDto.AuditLogSummaryDto mapAuditDto(IntegrationAuditLog log) {
        return ControlCenterSummaryDto.AuditLogSummaryDto.builder()
            .id(log.getId())
            .providerName(log.getProviderName())
            .actionType(log.getActionType())
            .status(log.getStatus())
            .requestSummary(log.getRequestSummary())
            .responseSummary(log.getResponseSummary())
            .executionTimeMs(log.getExecutionTimeMs())
            .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
            .build();
    }
}
