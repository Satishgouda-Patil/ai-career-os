package com.ai.career.execution.service;

import com.ai.career.domain.entity.User;
import com.ai.career.execution.dto.*;

public interface LiveBrowserExecutionService {
    LiveExecutionPrepareResponse prepareLiveExecution(User user, Long applicationId);
    LiveExecutionConfirmResponse confirmLiveExecution(User user, Long applicationId, String formFingerprint);
    LiveExecutionExecuteResponse executeLiveSubmission(User user, Long applicationId, String formFingerprint);
    LiveExecutionStatusResponse getLiveExecutionStatus(User user, Long applicationId);
}
