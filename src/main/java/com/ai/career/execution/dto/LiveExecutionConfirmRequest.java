package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveExecutionConfirmRequest {
    private String formFingerprint;
    private String previewId;
    private boolean candidateConfirmation;
}
