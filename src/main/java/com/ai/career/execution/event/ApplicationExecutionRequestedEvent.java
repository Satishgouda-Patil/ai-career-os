package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationExecutionRequestedEvent extends BaseApplicationEvent {
    private final String providerName;
    private final String idempotencyKey;
}
