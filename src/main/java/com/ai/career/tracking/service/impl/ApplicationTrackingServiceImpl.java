package com.ai.career.tracking.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.dto.ApplicationTrackingSummary;
import com.ai.career.tracking.service.ApplicationTimelineService;
import com.ai.career.tracking.service.ApplicationTrackingService;
import com.ai.career.tracking.service.NextActionDecision;
import com.ai.career.tracking.service.NextActionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationTrackingServiceImpl implements ApplicationTrackingService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationTimelineService timelineService;
    private final NextActionEngine nextActionEngine;

    @Override
    @Transactional(readOnly = true)
    public ApplicationTrackingSummary getTrackingSummary(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        List<ActivityDto> timeline = timelineService.getTimeline(userId, applicationId);
        NextActionDecision nextAction = nextActionEngine.evaluateNextAction(application, timeline);

        LocalDateTime appliedAt = application.getSubmittedAt();
        Long ageInDays = null;
        if (appliedAt != null) {
            ageInDays = Duration.between(appliedAt, LocalDateTime.now()).toDays();
        } else if (application.getCreatedAt() != null) {
            ageInDays = Duration.between(application.getCreatedAt(), LocalDateTime.now()).toDays();
        }

        LocalDateTime lastActivityAt = timeline.stream()
                .map(ActivityDto::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(application.getUpdatedAt() != null ? application.getUpdatedAt() : application.getCreatedAt());

        return ApplicationTrackingSummary.builder()
                .applicationId(application.getId())
                .jobId(application.getJob() != null ? application.getJob().getId() : null)
                .jobTitle(application.getJob() != null ? application.getJob().getTitle() : null)
                .company(application.getJob() != null ? application.getJob().getCompany() : null)
                .currentStatus(application.getStatus() != null ? application.getStatus().name() : "UNKNOWN")
                .appliedAt(appliedAt)
                .ageInDays(ageInDays)
                .lastActivityAt(lastActivityAt)
                .nextActionDecision(nextAction)
                .timeline(timeline)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NextActionDecision getNextAction(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        List<ActivityDto> timeline = timelineService.getTimeline(userId, applicationId);
        return nextActionEngine.evaluateNextAction(application, timeline);
    }
}
