package com.ai.career.execution.service;

import com.ai.career.domain.entity.User;
import com.ai.career.execution.dto.*;

import java.util.Map;

public interface ControlledApplicationExecutionService {

    ControlledExecutionStepResponse startExecution(User user, Long applicationId, ControlledExecutionStartRequest request);

    ControlledExecutionStepResponse inspectApplication(User user, Long applicationId);

    ControlledExecutionStepResponse mapCandidateFields(User user, Long applicationId);

    ControlledExecutionStepResponse runSandboxVerification(User user, Long applicationId);

    ControlledExecutionStepResponse prepareProductionExecution(User user, Long applicationId);

    ControlledExecutionSafetyReviewResponse getSafetyReview(User user, Long applicationId);

    ControlledExecutionStepResponse confirmCandidate(User user, Long applicationId, ControlledExecutionConfirmRequest request);

    ControlledExecutionStepResponse execute(User user, Long applicationId, ControlledExecutionRunRequest request);

    ControlledExecutionStepResponse verify(User user, Long applicationId);

    ControlledExecutionStatusResponse getStatus(User user, Long applicationId);

    Map<String, Object> getEvidence(User user, Long applicationId);

    ControlledExecutionStepResponse runFullPipeline(User user, Long applicationId, ControlledExecutionRunRequest request);
}
