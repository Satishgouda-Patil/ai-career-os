package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationExecutionCompletedEvent extends BaseApplicationEvent {
    private final String outcomeStatus;
}
