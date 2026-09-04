package com.ai.career.execution.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionSafetyReviewResponse {
    private Long applicationId;
    private String provider;
    private String targetUrl;
    private int fieldsDetected;
    private int fieldsMapped;
    private int fieldsRequireReview;
    private String executionMode;
    private boolean allowLiveSubmission;
    private boolean autoApply;
    private boolean autoSendEmail;
    private boolean autoLinkedIn;
    private List<SafetyCheckItem> safetyChecks;
    private boolean readyForConfirmation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyCheckItem {
        private String code;
        private String description;
        private boolean passed;
        private String message;
    }
}
