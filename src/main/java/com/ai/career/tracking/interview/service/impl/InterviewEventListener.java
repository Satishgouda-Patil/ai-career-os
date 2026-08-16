package com.ai.career.tracking.interview.service.impl;

import com.ai.career.tracking.email.event.InterviewDetectedEvent;
import com.ai.career.tracking.interview.dto.ScheduleInterviewRequest;
import com.ai.career.tracking.interview.service.InterviewPrepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewEventListener {

    private final InterviewPrepService prepService;

    @EventListener
    public void handleInterviewDetected(InterviewDetectedEvent event) {
        log.info("InterviewDetectedEvent received for App ID {}. Auto-creating interview & prep kit.", event.getApplicationId());

        ScheduleInterviewRequest request = ScheduleInterviewRequest.builder()
                .interviewType("INTERVIEW")
                .scheduledAt(LocalDateTime.now().plusDays(3))
                .timezone("UTC")
                .meetingUrl(event.getMeetingUrl())
                .notes("Automatically created from detected interview email.")
                .build();

        try {
            prepService.createInterview(event.getUserId(), event.getApplicationId(), request);
            log.info("Auto-created interview and preparation kit for App ID {}", event.getApplicationId());
        } catch (Exception e) {
            log.error("Failed to auto-create interview workspace for App ID {}: {}", event.getApplicationId(), e.getMessage());
        }
    }
}
