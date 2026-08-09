package com.ai.career.application.domain.entity;

public enum ApplicationExecutionStatus {
    READY,
    VALIDATING,
    LOCKED,
    PROVIDER_SELECTED,
    SESSION_STARTED,
    FORM_DISCOVERED,
    FORM_MAPPED,
    FORM_VALIDATED,
    READY_TO_SUBMIT,
    SUBMITTING,
    VERIFYING,
    SUBMITTED,
    FAILED,
    ACTION_REQUIRED,
    BLOCKED,
    CANCELLED,
    UNVERIFIED
}
