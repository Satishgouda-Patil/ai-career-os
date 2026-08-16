package com.ai.career.execution.service;

import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.WorkflowOrchestrationResponse;

import java.util.List;

public interface ApplicationOrchestratorService {
    WorkflowOrchestrationResponse orchestrate(Long userId, Long applicationId, ExecuteApplicationRequest request);
    WorkflowOrchestrationResponse approveAndPrepare(Long userId, Long applicationId, String approvedBy, String reason);
    ApplicationReadinessResult getReadiness(Long userId, Long applicationId);
    List<WorkflowOrchestrationResponse> getWorkflowHistory(Long userId, Long applicationId);
}
