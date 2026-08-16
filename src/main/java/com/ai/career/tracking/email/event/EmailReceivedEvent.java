package com.ai.career.tracking.email.event;

import com.ai.career.execution.event.BaseApplicationEvent;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EmailReceivedEvent extends BaseApplicationEvent {
    private final Long messageId;
    private final String provider;
    private final String externalMessageId;
    private final String externalThreadId;
    private final String sender;
    private final String subject;
}
