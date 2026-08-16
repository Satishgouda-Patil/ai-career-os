package com.ai.career.tracking.followup.service;

import com.ai.career.tracking.followup.dto.FollowUpDto;

public interface FollowUpGeneratorService {
    FollowUpDto generateFollowUpDraft(Long userId, Long applicationId, Integer sequenceNumber, String customNotes);
}
