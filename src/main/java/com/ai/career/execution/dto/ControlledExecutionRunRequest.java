package com.ai.career.execution.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionRunRequest {
    private String mode;
    private String previewId;
    private String confirmationId;
    private String formFingerprint;
}
