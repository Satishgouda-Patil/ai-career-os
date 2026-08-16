package com.ai.career.tracking.service.impl;

import com.ai.career.execution.event.*;
import com.ai.career.tracking.event.ApplicationAppliedEvent;
import com.ai.career.tracking.service.ApplicationTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationActivityEventListener {

    private final ApplicationTimelineService timelineService;

    @EventListener
    public void handleApplicationApproved(ApplicationApprovedEvent event) {
        log.info("Event received: ApplicationApprovedEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "APPROVED",
                "SYSTEM",
                "Application approved by user",
                Map.of("approvedBy", event.getApprovedBy()),
                1.0
        );
    }

    @EventListener
    public void handleExecutionStarted(ApplicationExecutionStartedEvent event) {
        log.info("Event received: ApplicationExecutionStartedEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "APPLYING",
                "SYSTEM",
                "Application submission process started",
                Map.of("providerName", event.getProviderName() != null ? event.getProviderName() : "GENERIC"),
                1.0
        );
    }

    @EventListener
    public void handleExecutionCompleted(ApplicationExecutionCompletedEvent event) {
        log.info("Event received: ApplicationExecutionCompletedEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "APPLIED",
                "SYSTEM",
                "Application successfully submitted",
                Map.of("outcomeStatus", event.getOutcomeStatus() != null ? event.getOutcomeStatus() : "SUCCESS"),
                1.0
        );
    }

    @EventListener
    public void handleExecutionRequiresReview(ApplicationExecutionRequiresReviewEvent event) {
        log.info("Event received: ApplicationExecutionRequiresReviewEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "SUBMISSION_REQUIRES_REVIEW",
                "SYSTEM",
                "Application execution requires human review",
                Map.of("reason", event.getReason() != null ? event.getReason() : "Action required"),
                1.0
        );
    }

    @EventListener
    public void handleExecutionFailed(ApplicationExecutionFailedEvent event) {
        log.info("Event received: ApplicationExecutionFailedEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "EXECUTION_FAILED",
                "SYSTEM",
                "Application execution failed: " + event.getErrorMessage(),
                Map.of("errorCode", event.getErrorCode() != null ? event.getErrorCode() : "UNKNOWN"),
                1.0
        );
    }

    @EventListener
    public void handleApplicationApplied(ApplicationAppliedEvent event) {
        log.info("Event received: ApplicationAppliedEvent for App ID {}", event.getApplicationId());
        timelineService.recordActivity(
                event.getUserId(),
                event.getApplicationId(),
                "CONFIRMATION_RECORDED",
                "SYSTEM",
                "Application status marked as APPLIED",
                Map.of("jobId", event.getJobId() != null ? event.getJobId() : 0L),
                1.0
        );
    }
}
