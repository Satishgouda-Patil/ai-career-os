package com.ai.career.execution.dto;

import com.ai.career.application.domain.entity.ApplicationExecutionStatus;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.execution.provider.ExecutionOutcomeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private Long id;
    private Long applicationId;
    private String providerName;
    private ApplicationExecutionStatus executionStatus;
    private ExecutionOutcomeStatus outcomeStatus;
    private ApplicationState applicationStatus;
    private String externalApplicationId;
    private String externalUrl;
    private String errorCode;
    private String errorMessage;
    private boolean retryable;
    private String executionLogs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
