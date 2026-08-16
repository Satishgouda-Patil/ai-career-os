package com.ai.career.tracking.followup.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.tracking.followup.domain.entity.ApplicationFollowUp;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
import com.ai.career.tracking.followup.dto.FollowUpDto;
import com.ai.career.tracking.followup.event.ApplicationFollowUpCancelledEvent;
import com.ai.career.tracking.followup.event.ApplicationFollowUpSentEvent;
import com.ai.career.tracking.followup.service.FollowUpGeneratorService;
import com.ai.career.tracking.followup.service.FollowUpSchedulerService;
import com.ai.career.tracking.service.ApplicationTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpSchedulerServiceImpl implements FollowUpSchedulerService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFollowUpRepository followUpRepository;
    private final FollowUpGeneratorService generatorService;
    private final ApplicationTimelineService timelineService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int MAX_FOLLOW_UPS = 2;

    @Override
    @Transactional
    public FollowUpDto scheduleNextFollowUp(Long userId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (userId != null && !app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        // Do not schedule if application is in terminal or response states
        if (app.getStatus() == ApplicationState.REJECTED || app.getStatus() == ApplicationState.WITHDRAWN || app.getStatus() == ApplicationState.INTERVIEW || app.getStatus() == ApplicationState.OFFER) {
            log.info("Skipping follow-up scheduling for Application ID {} in state {}", applicationId, app.getStatus());
            autoCancelPendingFollowUps(applicationId, "Application in terminal/response state: " + app.getStatus());
            return null;
        }

        List<ApplicationFollowUp> existing = followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(applicationId);
        int nextSeq = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getSequenceNumber() + 1;

        if (nextSeq > MAX_FOLLOW_UPS) {
            log.info("Max follow-up limit ({}) reached for Application ID {}", MAX_FOLLOW_UPS, applicationId);
            return mapToDto(existing.get(existing.size() - 1));
        }

        LocalDateTime scheduledAt = (nextSeq == 1) ? LocalDateTime.now().plusDays(3) : LocalDateTime.now().plusDays(5);

        ApplicationFollowUp followUp = ApplicationFollowUp.builder()
                .application(app)
                .channel("EMAIL")
                .sequenceNumber(nextSeq)
                .scheduledAt(scheduledAt)
                .status("SCHEDULED")
                .build();

        followUp = followUpRepository.save(followUp);
        log.info("Scheduled Follow-up #{} for App ID {} at {}", nextSeq, applicationId, scheduledAt);

        // Auto generate draft
        generatorService.generateFollowUpDraft(app.getUser().getId(), applicationId, nextSeq, null);

        return mapToDto(followUpRepository.findById(followUp.getId()).orElse(followUp));
    }

    @Override
    @Transactional
    public void autoCancelPendingFollowUps(Long applicationId, String reason) {
        List<ApplicationFollowUp> followUps = followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(applicationId);
        for (ApplicationFollowUp f : followUps) {
            if ("SCHEDULED".equalsIgnoreCase(f.getStatus()) || "READY".equalsIgnoreCase(f.getStatus()) || "APPROVAL_REQUIRED".equalsIgnoreCase(f.getStatus())) {
                f.setStatus("CANCELLED");
                followUpRepository.save(f);
                log.info("Auto-cancelled Follow-up ID {} for App ID {}: Reason: '{}'", f.getId(), applicationId, reason);

                eventPublisher.publishEvent(ApplicationFollowUpCancelledEvent.builder()
                        .followUpId(f.getId())
                        .applicationId(applicationId)
                        .userId(f.getApplication().getUser().getId())
                        .reason(reason)
                        .correlationId(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .build());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpDto> getApplicationFollowUps(Long userId, Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!app.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        return followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(applicationId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpDto> getUserFollowUps(Long userId) {
        List<Application> userApps = applicationRepository.findByUserId(userId);
        return userApps.stream()
                .flatMap(app -> followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(app.getId()).stream())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FollowUpDto approveFollowUp(Long userId, Long followUpId) {
        ApplicationFollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up not found with ID: " + followUpId));

        if (!followUp.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to follow-up ID: " + followUpId);
        }

        followUp.setStatus("READY");
        followUp.setApprovedAt(LocalDateTime.now());
        followUp = followUpRepository.save(followUp);

        return mapToDto(followUp);
    }

    @Override
    @Transactional
    public FollowUpDto sendFollowUp(Long userId, Long followUpId) {
        ApplicationFollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up not found with ID: " + followUpId));

        if (!followUp.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to follow-up ID: " + followUpId);
        }

        followUp.setStatus("SENT");
        followUp.setSentAt(LocalDateTime.now());
        followUp = followUpRepository.save(followUp);

        // Record timeline activity
        timelineService.recordActivity(
                userId,
                followUp.getApplication().getId(),
                "FOLLOW_UP_SENT",
                "USER",
                "Follow-up #" + followUp.getSequenceNumber() + " sent: " + followUp.getFollowUpSubject(),
                Map.of("followUpId", followUp.getId(), "sequenceNumber", followUp.getSequenceNumber()),
                1.0
        );

        eventPublisher.publishEvent(ApplicationFollowUpSentEvent.builder()
                .followUpId(followUp.getId())
                .applicationId(followUp.getApplication().getId())
                .userId(userId)
                .sequenceNumber(followUp.getSequenceNumber())
                .channel(followUp.getChannel())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToDto(followUp);
    }

    @Override
    @Transactional
    public FollowUpDto cancelFollowUp(Long userId, Long followUpId, String reason) {
        ApplicationFollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up not found with ID: " + followUpId));

        if (!followUp.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to follow-up ID: " + followUpId);
        }

        followUp.setStatus("CANCELLED");
        followUp = followUpRepository.save(followUp);

        eventPublisher.publishEvent(ApplicationFollowUpCancelledEvent.builder()
                .followUpId(followUp.getId())
                .applicationId(followUp.getApplication().getId())
                .userId(userId)
                .reason(reason != null ? reason : "Cancelled by user")
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToDto(followUp);
    }

    private FollowUpDto mapToDto(ApplicationFollowUp followUp) {
        return FollowUpDto.builder()
                .id(followUp.getId())
                .applicationId(followUp.getApplication().getId())
                .channel(followUp.getChannel())
                .sequenceNumber(followUp.getSequenceNumber())
                .scheduledAt(followUp.getScheduledAt())
                .status(followUp.getStatus())
                .messageArtifactId(followUp.getMessageArtifactId())
                .followUpSubject(followUp.getFollowUpSubject())
                .followUpBody(followUp.getFollowUpBody())
                .sentAt(followUp.getSentAt())
                .approvedAt(followUp.getApprovedAt())
                .createdAt(followUp.getCreatedAt())
                .build();
    }
}
