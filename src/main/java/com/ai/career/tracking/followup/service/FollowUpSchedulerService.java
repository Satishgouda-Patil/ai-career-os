package com.ai.career.tracking.followup.service;

import com.ai.career.tracking.followup.dto.FollowUpDto;

import java.util.List;

public interface FollowUpSchedulerService {
    FollowUpDto scheduleNextFollowUp(Long userId, Long applicationId);
    void autoCancelPendingFollowUps(Long applicationId, String reason);
    List<FollowUpDto> getApplicationFollowUps(Long userId, Long applicationId);
    List<FollowUpDto> getUserFollowUps(Long userId);
    FollowUpDto approveFollowUp(Long userId, Long followUpId);
    FollowUpDto sendFollowUp(Long userId, Long followUpId);
    FollowUpDto cancelFollowUp(Long userId, Long followUpId, String reason);
}
