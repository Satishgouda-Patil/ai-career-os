package com.ai.career.tracking.service;

import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.dto.RecordActivityRequest;

import java.util.List;
import java.util.Map;

public interface ApplicationTimelineService {
    ActivityDto recordActivity(Long userId, Long applicationId, String activityType, String source, String description, Map<String, Object> metadata, Double confidence);
    ActivityDto recordActivity(Long userId, Long applicationId, RecordActivityRequest request);
    List<ActivityDto> getTimeline(Long userId, Long applicationId);
}
