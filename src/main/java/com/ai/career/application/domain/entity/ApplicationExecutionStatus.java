package com.ai.career.application.domain.entity;

public enum ApplicationExecutionStatus {
    PENDING,
    VALIDATING,
    RUNNING,
    ACTION_REQUIRED,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    UNKNOWN;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == CANCELLED;
    }
}
