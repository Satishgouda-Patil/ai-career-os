package com.ai.career.execution.browser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowserDiscoveryResultDto {
    private Long applicationId;
    private String status; // "DISCOVERED", "DOMAIN_NOT_ALLOWED", etc.
    private String executionMode; // "READ_ONLY"
    private boolean submissionAttempted; // false
    private String targetUrl;
    private String finalUrl;
    private int formsDetected;
    private int fieldsDetected;
    private List<DiscoveredFieldDto> fields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscoveredFieldDto {
        private String fieldSelector;
        private String originalFieldId;
        private String name;
        private String label;
        private String normalizedLabel;
        private String type; // "TEXT", "EMAIL", "TEL", "FILE", "SELECT", etc.
        private boolean required;
        private List<String> options;
        private String accept;
    }
}
