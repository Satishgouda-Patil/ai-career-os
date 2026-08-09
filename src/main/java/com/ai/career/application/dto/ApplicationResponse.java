package com.ai.career.application.dto;

import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.job.dto.JobDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private JobDto job;
    private Long workspaceId;
    private Long resumeVersionId;
    private Long coverLetterId;
    private ApplicationState status;
    private String applicationMethod;
    private BigDecimal matchScore;
    private BigDecimal atsScore;
    private String recommendation;
    private String applicationUrl;
    private String providerName;
    private String providerApplicationId;
    private String automationLevel;
    private boolean approvalRequired;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime closedAt;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ApplicationHistoryResponse> history;
}
