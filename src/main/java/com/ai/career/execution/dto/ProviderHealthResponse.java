package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderHealthResponse {

    @Builder.Default
    private List<ProviderHealthItem> providers = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderHealthItem {
        private String provider;
        private String status;
        private boolean reachable;
        private boolean readOnlySupported;
        private boolean liveExecutionEnabled;
        private String category;
        private String rateLimitInfo;
    }
}
