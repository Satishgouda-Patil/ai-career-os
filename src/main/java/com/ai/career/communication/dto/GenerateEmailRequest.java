package com.ai.career.communication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateEmailRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    private Long recruiterId;
}
