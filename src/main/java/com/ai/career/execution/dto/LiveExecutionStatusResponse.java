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
public class LiveExecutionStatusResponse {
    private Long applicationId;
    private String mode;
    private String provider;
    private String state;
    private String executionStatus;
    private Long lastAuditId;
    private boolean submissionAttempted;
    private boolean submissionVerified;
    private boolean allowLiveSubmission;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
