package com.ai.career.tracking.service;

import com.ai.career.tracking.dto.ApplicationTrackingSummary;

public interface ApplicationTrackingService {
    ApplicationTrackingSummary getTrackingSummary(Long userId, Long applicationId);
    NextActionDecision getNextAction(Long userId, Long applicationId);
}
