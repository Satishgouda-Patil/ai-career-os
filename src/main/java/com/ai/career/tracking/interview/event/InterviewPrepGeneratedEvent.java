package com.ai.career.tracking.interview.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class InterviewPrepGeneratedEvent extends BaseApplicationEvent {
    private final Long interviewId;
    private final Long prepId;
}
