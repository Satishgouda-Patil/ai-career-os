package com.ai.career.application.statemachine;

import com.ai.career.application.domain.entity.ApplicationState;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class ApplicationStateMachine {

    private static final Map<ApplicationState, Set<ApplicationState>> LEGAL_TRANSITIONS = Map.ofEntries(
        Map.entry(ApplicationState.DISCOVERED, EnumSet.of(ApplicationState.QUALIFIED, ApplicationState.PREPARING, ApplicationState.CLOSED, ApplicationState.REJECTED)),
        Map.entry(ApplicationState.QUALIFIED, EnumSet.of(ApplicationState.PREPARING, ApplicationState.CLOSED, ApplicationState.REJECTED)),
        Map.entry(ApplicationState.PREPARING, EnumSet.of(ApplicationState.READY_FOR_REVIEW, ApplicationState.FAILED, ApplicationState.CLOSED, ApplicationState.REJECTED)),
        Map.entry(ApplicationState.READY_FOR_REVIEW, EnumSet.of(ApplicationState.APPROVED, ApplicationState.REJECTED, ApplicationState.ACTION_REQUIRED, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.APPROVED, EnumSet.of(ApplicationState.APPLYING, ApplicationState.REJECTED, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.APPLYING, EnumSet.of(ApplicationState.APPLIED, ApplicationState.SUBMISSION_REQUIRES_REVIEW, ApplicationState.FAILED, ApplicationState.ACTION_REQUIRED)),
        Map.entry(ApplicationState.SUBMISSION_REQUIRES_REVIEW, EnumSet.of(ApplicationState.APPROVED, ApplicationState.REJECTED, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.ACTION_REQUIRED, EnumSet.of(ApplicationState.PREPARING, ApplicationState.READY_FOR_REVIEW, ApplicationState.APPLYING, ApplicationState.CLOSED, ApplicationState.REJECTED)),
        Map.entry(ApplicationState.APPLIED, EnumSet.of(ApplicationState.RESPONDED, ApplicationState.INTERVIEW, ApplicationState.NO_RESPONSE, ApplicationState.REJECTED, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.RESPONDED, EnumSet.of(ApplicationState.INTERVIEW, ApplicationState.OFFER, ApplicationState.REJECTED, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.INTERVIEW, EnumSet.of(ApplicationState.OFFER, ApplicationState.REJECTED, ApplicationState.WITHDRAWN, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.OFFER, EnumSet.of(ApplicationState.CLOSED, ApplicationState.WITHDRAWN)),
        Map.entry(ApplicationState.FAILED, EnumSet.of(ApplicationState.PREPARING, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.REJECTED, EnumSet.of(ApplicationState.PREPARING, ApplicationState.CLOSED)),
        Map.entry(ApplicationState.WITHDRAWN, EnumSet.of(ApplicationState.CLOSED)),
        Map.entry(ApplicationState.NO_RESPONSE, EnumSet.of(ApplicationState.CLOSED)),
        Map.entry(ApplicationState.CLOSED, EnumSet.of(ApplicationState.PREPARING))
    );

    public boolean canTransition(ApplicationState currentState, ApplicationState targetState) {
        if (currentState == targetState) {
            return true;
        }
        Set<ApplicationState> allowedTargets = LEGAL_TRANSITIONS.get(currentState);
        return allowedTargets != null && allowedTargets.contains(targetState);
    }

    public void validateTransition(ApplicationState currentState, ApplicationState targetState) {
        if (!canTransition(currentState, targetState)) {
            throw new IllegalStateException(String.format(
                "Illegal application state transition from '%s' to '%s'", currentState, targetState
            ));
        }
    }
}
