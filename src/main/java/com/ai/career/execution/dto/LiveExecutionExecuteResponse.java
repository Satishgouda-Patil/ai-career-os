package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveExecutionExecuteResponse {
    private Long applicationId;
    private String mode;
    private String provider;
    private String status;
    private boolean submissionAttempted;
    private boolean submissionVerified;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime executedAt;
    private Long lastAuditId;
}
