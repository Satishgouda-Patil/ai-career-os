package com.ai.career.tracking.followup.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationFollowUpCancelledEvent extends BaseApplicationEvent {
    private final Long followUpId;
    private final String reason;
}
