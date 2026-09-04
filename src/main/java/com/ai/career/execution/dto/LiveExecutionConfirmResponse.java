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
public class LiveExecutionConfirmResponse {
    private Long applicationId;
    private String status;
    private String formFingerprint;
    private String previewId;
    private LocalDateTime confirmedAt;
    private String message;
}
