package com.ai.career.execution.service.impl;

import com.ai.career.execution.dto.SafetyConfigurationResponse;
import com.ai.career.execution.gate.LiveSubmissionSafetyGate;
import com.ai.career.execution.service.ExecutionSafetyConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionSafetyConfigurationServiceImpl implements ExecutionSafetyConfigurationService {

    private final LiveSubmissionSafetyGate safetyGate;

    @Override
    public SafetyConfigurationResponse getEffectiveSafetyConfiguration() {
        return SafetyConfigurationResponse.builder()
                .autoApply(false)
                .autoSendEmail(false)
                .autoLinkedIn(false)
                .allowLiveSubmission(safetyGate.isLiveSubmissionAllowed())
                .executionMode("PRODUCTION_READ_ONLY")
                .build();
    }
}
