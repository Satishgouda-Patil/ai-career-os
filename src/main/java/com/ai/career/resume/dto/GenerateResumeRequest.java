package com.ai.career.resume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResumeRequest {

    private Long jobId;

    @Builder.Default
    private String template = "MODERN";
}
