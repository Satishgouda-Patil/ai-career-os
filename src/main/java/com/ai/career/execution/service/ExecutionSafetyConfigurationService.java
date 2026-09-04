package com.ai.career.execution.service;

import com.ai.career.execution.dto.SafetyConfigurationResponse;

public interface ExecutionSafetyConfigurationService {
    SafetyConfigurationResponse getEffectiveSafetyConfiguration();
}
