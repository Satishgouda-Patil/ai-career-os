package com.ai.career.application.domain.entity;

public enum ApplicationState {
    DISCOVERED,
    QUALIFIED,
    PREPARING,
    READY_FOR_REVIEW,
    APPROVED,
    CONFIRMED_SUBMISSION,
    APPLYING,
    SUBMISSION_REQUIRES_REVIEW,
    APPLIED,
    RESPONDED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN,
    NO_RESPONSE,
    CLOSED,
    FAILED,
    ACTION_REQUIRED;

    public boolean isActive() {
        return this != OFFER &&
               this != REJECTED &&
               this != WITHDRAWN &&
               this != NO_RESPONSE &&
               this != CLOSED &&
               this != FAILED;
    }
}
