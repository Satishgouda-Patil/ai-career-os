package com.ai.career.tracking.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationAppliedEvent extends BaseApplicationEvent {
    private final Long jobId;
}
