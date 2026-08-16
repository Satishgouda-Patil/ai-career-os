package com.ai.career.tracking.email.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class JobEmailClassifiedEvent extends BaseApplicationEvent {
    private final Long messageId;
    private final String classification;
    private final Double confidence;
}
