package com.ai.career.execution.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionConfirmRequest {
    private String previewId;
    private String formFingerprint;
    private Boolean acknowledged;
}
