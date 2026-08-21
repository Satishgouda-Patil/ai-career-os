package com.ai.career.execution.sandbox.service;

import com.ai.career.execution.sandbox.dto.SandboxExecutionResultDto;

public interface SandboxExecutionService {

    SandboxExecutionResultDto executeSandbox(Long applicationId, Long userId);

    SandboxExecutionResultDto getLatestSandboxStatus(Long applicationId, Long userId);
}
