package com.ai.career.tracking.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.tracking.domain.entity.ApplicationActivity;
import com.ai.career.tracking.domain.repository.ApplicationActivityRepository;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.dto.RecordActivityRequest;
import com.ai.career.tracking.event.ApplicationActivityRecordedEvent;
import com.ai.career.tracking.service.ApplicationTimelineService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationTimelineServiceImpl implements ApplicationTimelineService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationActivityRepository activityRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ActivityDto recordActivity(Long userId, Long applicationId, String activityType, String source, String description, Map<String, Object> metadata, Double confidence) {
        log.info("Recording application activity: Application ID: {}, ActivityType: {}, Source: {}", applicationId, activityType, source);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (userId != null && !application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (Exception e) {
                log.warn("Failed to serialize activity metadata for Application ID {}", applicationId, e);
            }
        }

        ApplicationActivity activity = ApplicationActivity.builder()
                .application(application)
                .activityType(activityType)
                .source(source != null ? source : "SYSTEM")
                .description(description)
                .metadataJson(metadataJson)
                .confidence(confidence != null ? confidence : 1.0)
                .build();

        activity = activityRepository.save(activity);

        eventPublisher.publishEvent(ApplicationActivityRecordedEvent.builder()
                .activityId(activity.getId())
                .applicationId(applicationId)
                .userId(application.getUser().getId())
                .activityType(activityType)
                .source(activity.getSource())
                .confidence(activity.getConfidence())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToDto(activity);
    }

    @Override
    @Transactional
    public ActivityDto recordActivity(Long userId, Long applicationId, RecordActivityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("RecordActivityRequest must not be null");
        }
        return recordActivity(
                userId,
                applicationId,
                request.getActivityType(),
                request.getSource(),
                request.getDescription(),
                request.getMetadata(),
                request.getConfidence()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDto> getTimeline(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        return activityRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ActivityDto mapToDto(ApplicationActivity activity) {
        Map<String, Object> metadataMap = Collections.emptyMap();
        if (activity.getMetadataJson() != null && !activity.getMetadataJson().isBlank()) {
            try {
                metadataMap = objectMapper.readValue(activity.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize metadata JSON for activity ID {}", activity.getId());
            }
        }

        return ActivityDto.builder()
                .id(activity.getId())
                .applicationId(activity.getApplication().getId())
                .activityType(activity.getActivityType())
                .source(activity.getSource())
                .description(activity.getDescription())
                .metadata(metadataMap)
                .confidence(activity.getConfidence())
                .createdAt(activity.getCreatedAt() != null ? activity.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
