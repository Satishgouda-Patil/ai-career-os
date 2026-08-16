package com.ai.career.integration.service;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.integration.domain.entity.IntegrationAuditLog;
import com.ai.career.integration.domain.repository.IntegrationAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationAuditService {

    private final IntegrationAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private static final Pattern SECRET_PATTERN = Pattern.compile(
        "(?i)(api[-_]?key|password|token|secret|auth|bearer|cookie)=[\"']?[^\"'\\s&]+[\"']?"
    );

    public String sanitize(String input) {
        if (input == null) return null;
        return SECRET_PATTERN.matcher(input).replaceAll("$1=***SANITIZED***");
    }

    @Transactional
    public IntegrationAuditLog recordAudit(
            Long userId,
            Long applicationId,
            String providerName,
            String actionType,
            String status,
            String requestSummary,
            String responseSummary,
            Long executionTimeMs,
            String errorCode) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found for audit logging with ID: " + userId));

        IntegrationAuditLog logEntry = IntegrationAuditLog.builder()
            .user(user)
            .applicationId(applicationId)
            .providerName(providerName)
            .actionType(actionType)
            .status(status)
            .requestSummary(sanitize(requestSummary))
            .responseSummary(sanitize(responseSummary))
            .executionTimeMs(executionTimeMs)
            .errorCode(errorCode)
            .build();

        log.info("Recording integration audit log: user={}, provider={}, action={}, status={}",
            userId, providerName, actionType, status);

        return auditLogRepository.save(logEntry);
    }
}
