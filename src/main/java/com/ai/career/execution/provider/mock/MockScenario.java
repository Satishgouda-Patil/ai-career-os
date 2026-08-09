package com.ai.career.execution.provider.mock;

public enum MockScenario {
    SUCCESS,
    VALIDATION_FAILURE,
    REQUIRES_HUMAN,
    UNSUPPORTED,
    RETRYABLE_FAILURE,
    NON_RETRYABLE_FAILURE,
    UNKNOWN
}
