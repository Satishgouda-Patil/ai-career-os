package com.ai.career.tracking.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationActivityRecordedEvent extends BaseApplicationEvent {
    private final Long activityId;
    private final String activityType;
    private final String source;
    private final Double confidence;
}
