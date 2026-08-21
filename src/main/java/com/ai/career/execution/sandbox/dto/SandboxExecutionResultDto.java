package com.ai.career.execution.sandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxExecutionResultDto {
    private Long applicationId;
    @Builder.Default
    private String executionMode = "SANDBOX";
    private String status;
    private int fieldsDetected;
    private int fieldsMapped;
    private int fieldsVerified;
    private int fieldsRequireReview;
    @Builder.Default
    private boolean submissionSimulated = true;
    @Builder.Default
    private boolean submissionVerified = true;
    @Builder.Default
    private boolean realSubmissionAttempted = false;
    @Builder.Default
    private boolean emailSent = false;
    @Builder.Default
    private boolean fileUploadedToRealProvider = false;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorCode;
}
