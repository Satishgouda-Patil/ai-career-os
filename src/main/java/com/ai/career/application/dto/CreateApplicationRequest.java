package com.ai.career.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;

    private Long workspaceId;
    private Long resumeVersionId;
    private Long coverLetterId;

    private String applicationMethod;
    private BigDecimal matchScore;
    private BigDecimal atsScore;
    private String recommendation;
    private String applicationUrl;
    private String providerName;
    private String automationLevel;
}
