package com.ai.career.execution.dto;

import com.ai.career.execution.service.ApplicationReadinessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowOrchestrationResponse {
    private Long workflowRunId;
    private Long applicationId;
    private String workflowType;
    private String status;
    private String idempotencyKey;
    private String correlationId;
    private String currentStage;
    private String failureCode;
    private int retryCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private ApplicationReadinessResult readiness;
}
