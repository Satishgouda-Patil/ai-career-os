package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionOperationsRunDto {
    private Long runId;
    private Long applicationId;
    private Long userId;
    private String provider;
    private String mode;
    private String status;
    private boolean submissionAttempted;
    private boolean submissionVerified;
    private boolean emailSent;
    private boolean fileUploaded;
    private String formFingerprint;
    private int fieldsDetected;
    private int fieldsMapped;
    private int fieldsRequireReview;
    private boolean retryEligible;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Builder.Default
    private List<String> safetyChecksPassed = new ArrayList<>();

    @Builder.Default
    private List<String> safetyChecksFailed = new ArrayList<>();
}
