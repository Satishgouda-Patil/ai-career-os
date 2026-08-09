package com.ai.career.execution.service;

import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.ExecutionResponse;
import com.ai.career.execution.provider.ExecutionValidationResult;

import java.util.List;

public interface ApplicationExecutionService {
    ExecutionResponse executeApplication(Long userId, Long applicationId, ExecuteApplicationRequest request);
    ExecutionValidationResult validateExecution(Long userId, Long applicationId, ExecuteApplicationRequest request);
    List<ExecutionResponse> getExecutionsByApplicationId(Long userId, Long applicationId);
    ExecutionResponse getExecutionById(Long userId, Long applicationId, Long executionId);
}
