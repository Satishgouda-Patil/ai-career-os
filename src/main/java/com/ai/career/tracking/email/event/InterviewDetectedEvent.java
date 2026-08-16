package com.ai.career.tracking.email.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class InterviewDetectedEvent extends BaseApplicationEvent {
    private final Long messageId;
    private final Double confidence;
    private final String interviewDate;
    private final String meetingUrl;
}
