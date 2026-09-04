package com.ai.career.execution.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlledExecutionStartRequest {
    private String mode;
}
