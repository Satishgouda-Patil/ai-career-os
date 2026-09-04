package com.ai.career.execution.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionStepResponse {
    private Long applicationId;
    private String executionRunId;
    private String previewId;
    private String confirmationId;
    private String formFingerprint;
    private String workflowStatus;
    private String executionMode;
    private int currentStep;
    private String provider;
    private boolean submissionAttempted;
    private boolean emailSent;
    private boolean fileUploaded;
    private String failureCode;
    private String failureReason;
    private String verificationStatus;
    private LocalDateTime timestamp;
    private Map<String, Object> stepDetails;
    private List<String> warnings;
}
