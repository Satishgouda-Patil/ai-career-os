package com.ai.career.tracking.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.service.NextActionDecision;
import com.ai.career.tracking.service.NextActionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class NextActionEngineImpl implements NextActionEngine {

    private static final int DEFAULT_FOLLOW_UP_DAYS = 3;

    @Override
    public NextActionDecision evaluateNextAction(Application application, List<ActivityDto> timeline) {
        if (application == null) {
            return NextActionDecision.builder()
                    .nextAction("NONE")
                    .dueDate(null)
                    .reason("Application is null")
                    .urgency("LOW")
                    .build();
        }

        ApplicationState state = application.getStatus();
        LocalDateTime now = LocalDateTime.now();

        // 1. Check for Terminal states
        if (state == ApplicationState.REJECTED || state == ApplicationState.WITHDRAWN) {
            return NextActionDecision.builder()
                    .nextAction("NONE")
                    .dueDate(null)
                    .reason("Application lifecycle closed (" + state + ")")
                    .urgency("LOW")
                    .build();
        }

        // 2. Check timeline for recent recruiter/interview activities
        Optional<ActivityDto> latestActivity = (timeline != null && !timeline.isEmpty())
                ? timeline.stream().max(Comparator.comparing(ActivityDto::getCreatedAt))
                : Optional.empty();

        if (latestActivity.isPresent()) {
            String type = latestActivity.get().getActivityType();
            if ("INTERVIEW_SCHEDULED".equalsIgnoreCase(type)) {
                return NextActionDecision.builder()
                        .nextAction("PREPARE_FOR_INTERVIEW")
                        .dueDate(now.plusDays(1))
                        .reason("Upcoming interview scheduled")
                        .urgency("HIGH")
                        .build();
            } else if ("RECRUITER_CONTACTED".equalsIgnoreCase(type) || "RECRUITER_RESPONDED".equalsIgnoreCase(type)) {
                return NextActionDecision.builder()
                        .nextAction("RESPOND_TO_RECRUITER")
                        .dueDate(now.plusDays(1))
                        .reason("Recruiter response requires attention")
                        .urgency("CRITICAL")
                        .build();
            } else if ("FOLLOW_UP_SENT".equalsIgnoreCase(type)) {
                return NextActionDecision.builder()
                        .nextAction("AWAIT_RECRUITER_RESPONSE")
                        .dueDate(latestActivity.get().getCreatedAt().plusDays(5))
                        .reason("Follow-up already sent; awaiting reply")
                        .urgency("LOW")
                        .build();
            }
        }

        // 3. State-based evaluation
        if (state == ApplicationState.READY_FOR_REVIEW) {
            return NextActionDecision.builder()
                    .nextAction("APPROVE_APPLICATION")
                    .dueDate(now.plusDays(1))
                    .reason("Application artifacts ready for review and approval")
                    .urgency("HIGH")
                    .build();
        } else if (state == ApplicationState.APPROVED) {
            return NextActionDecision.builder()
                    .nextAction("DISPATCH_EXECUTION")
                    .dueDate(now)
                    .reason("Application approved and ready for submission")
                    .urgency("HIGH")
                    .build();
        } else if (state == ApplicationState.DISCOVERED || state == ApplicationState.QUALIFIED || state == ApplicationState.PREPARING) {
            return NextActionDecision.builder()
                    .nextAction("GENERATE_ARTIFACTS")
                    .dueDate(now.plusDays(2))
                    .reason("Application preparation in progress")
                    .urgency("MEDIUM")
                    .build();
        } else if (state == ApplicationState.APPLIED) {
            LocalDateTime appliedAt = application.getSubmittedAt() != null
                    ? application.getSubmittedAt()
                    : application.getCreatedAt();

            long daysSinceApplied = appliedAt != null ? Duration.between(appliedAt, now).toDays() : 0;

            if (daysSinceApplied >= DEFAULT_FOLLOW_UP_DAYS) {
                return NextActionDecision.builder()
                        .nextAction("FOLLOW_UP_DUE")
                        .dueDate(now)
                        .reason("Applied " + daysSinceApplied + " days ago with no response")
                        .urgency("HIGH")
                        .build();
            } else {
                return NextActionDecision.builder()
                        .nextAction("AWAIT_RECRUITER_RESPONSE")
                        .dueDate(appliedAt.plusDays(DEFAULT_FOLLOW_UP_DAYS))
                        .reason("Applied recently; waiting for response")
                        .urgency("LOW")
                        .build();
            }
        }

        return NextActionDecision.builder()
                .nextAction("REVIEW_APPLICATION")
                .dueDate(now.plusDays(3))
                .reason("Application in status " + state)
                .urgency("MEDIUM")
                .build();
    }
}
