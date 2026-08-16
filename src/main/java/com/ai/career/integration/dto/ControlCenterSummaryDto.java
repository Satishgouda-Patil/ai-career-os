package com.ai.career.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlCenterSummaryDto {
    private String operatingMode; // e.g. "PRODUCTION / READ-ONLY"
    private SafetyFlagsDto safetyFlags;
    private List<ProviderHealthDto> providers;
    private List<AuditLogSummaryDto> recentAudits;
    private List<AuditLogSummaryDto> recentFailures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyFlagsDto {
        private boolean autoApply;
        private boolean autoSendEmail;
        private boolean autoLinkedIn;
        private boolean allowLiveSubmission;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderHealthDto {
        private String providerName;
        private String category;
        private String status; // "HEALTHY", "DEGRADED", "UNCONFIGURED"
        private boolean isSandbox;
        private String rateLimitInfo;
        private String lastSync;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogSummaryDto {
        private Long id;
        private String providerName;
        private String actionType;
        private String status;
        private String requestSummary;
        private String responseSummary;
        private Long executionTimeMs;
        private String createdAt;
    }
}
