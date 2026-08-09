package com.ai.career.validation.service;

import com.ai.career.validation.model.ExecutionAuthorization;

public interface ExecutionAuthorizationService {
    ExecutionAuthorization authorizeExecution(Long userId, Long applicationId, String note);
    ExecutionAuthorization checkAuthorization(Long userId, Long applicationId);
}
