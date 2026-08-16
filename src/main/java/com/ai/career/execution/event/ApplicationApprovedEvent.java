package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ApplicationApprovedEvent extends BaseApplicationEvent {
    private final String approvedBy;
}
