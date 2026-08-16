package com.ai.career.execution.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class ApplicationExecutionRequiresReviewEvent extends BaseApplicationEvent {
    private final String reason;
    private final List<String> reviewReasons;
}
