package com.ai.career.tracking.followup.service.impl;

import com.ai.career.execution.event.ApplicationExecutionCompletedEvent;
import com.ai.career.tracking.email.event.InterviewDetectedEvent;
import com.ai.career.tracking.email.event.RecruiterResponseDetectedEvent;
import com.ai.career.tracking.email.event.RejectionDetectedEvent;
import com.ai.career.tracking.event.ApplicationAppliedEvent;
import com.ai.career.tracking.followup.service.FollowUpSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUpEventListener {

    private final FollowUpSchedulerService schedulerService;

    @EventListener
    public void handleApplicationExecutionCompleted(ApplicationExecutionCompletedEvent event) {
        log.info("Application execution completed for App ID {}. Auto-scheduling Follow-up #1.", event.getApplicationId());
        schedulerService.scheduleNextFollowUp(event.getUserId(), event.getApplicationId());
    }

    @EventListener
    public void handleApplicationApplied(ApplicationAppliedEvent event) {
        log.info("Application applied event received for App ID {}. Auto-scheduling Follow-up #1.", event.getApplicationId());
        schedulerService.scheduleNextFollowUp(event.getUserId(), event.getApplicationId());
    }

    @EventListener
    public void handleRecruiterResponse(RecruiterResponseDetectedEvent event) {
        log.info("Recruiter response detected for App ID {}. Auto-cancelling pending follow-ups.", event.getApplicationId());
        schedulerService.autoCancelPendingFollowUps(event.getApplicationId(), "Recruiter responded via email");
    }

    @EventListener
    public void handleInterviewDetected(InterviewDetectedEvent event) {
        log.info("Interview detected for App ID {}. Auto-cancelling pending follow-ups.", event.getApplicationId());
        schedulerService.autoCancelPendingFollowUps(event.getApplicationId(), "Interview scheduled");
    }

    @EventListener
    public void handleRejectionDetected(RejectionDetectedEvent event) {
        log.info("Rejection notice detected for App ID {}. Auto-cancelling pending follow-ups.", event.getApplicationId());
        schedulerService.autoCancelPendingFollowUps(event.getApplicationId(), "Rejection notice received");
    }
}
