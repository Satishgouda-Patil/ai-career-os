package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public abstract class BaseApplicationEvent {
    private final Long applicationId;
    private final Long userId;
    private final String correlationId;
    private final LocalDateTime timestamp;
}
