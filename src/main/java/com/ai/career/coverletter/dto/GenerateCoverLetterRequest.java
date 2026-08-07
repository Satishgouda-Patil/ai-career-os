package com.ai.career.coverletter.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCoverLetterRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @Builder.Default
    private String tone = "Professional";
}
