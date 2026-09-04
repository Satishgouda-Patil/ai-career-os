package com.ai.career.execution.service;

import com.ai.career.domain.entity.User;
import com.ai.career.execution.dto.ExecutionMetricsResponse;
import com.ai.career.execution.dto.ExecutionOperationsRunDto;
import com.ai.career.execution.dto.RetryEligibilityResponse;

import java.util.List;

public interface BrowserExecutionOperationsService {
    List<ExecutionOperationsRunDto> getExecutionRuns(User user, Long applicationId, String status, String provider);
    ExecutionOperationsRunDto getExecutionRunDetail(User user, Long runId);
    RetryEligibilityResponse getRetryEligibility(User user, Long runId);
    ExecutionOperationsRunDto retryExecution(User user, Long runId);
    ExecutionMetricsResponse getExecutionMetrics(User user);
    int recoverStaleExecutions();
}
