package com.ai.career.execution.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionStatusResponse {
    private Long id;
    private Long applicationId;
    private String executionRunId;
    private String previewId;
    private String confirmationId;
    private String formFingerprint;
    private String workflowStatus;
    private String executionMode;
    private int currentStep;
    private String failureCode;
    private String failureReason;
    private String verificationStatus;
    private boolean submissionAttempted;
    private boolean emailSent;
    private boolean fileUploaded;
    private boolean allowLiveSubmission;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<String> warnings;
    private List<String> errors;
}
