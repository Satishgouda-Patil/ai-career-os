package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationExecutionFailedEvent extends BaseApplicationEvent {
    private final String errorCode;
    private final String errorMessage;
}
