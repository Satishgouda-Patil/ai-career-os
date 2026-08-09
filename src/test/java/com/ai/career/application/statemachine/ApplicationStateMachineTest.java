package com.ai.career.application.statemachine;

import com.ai.career.application.domain.entity.ApplicationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationStateMachineTest {

    private ApplicationStateMachine stateMachine;

    @BeforeEach
    public void setUp() {
        stateMachine = new ApplicationStateMachine();
    }

    @Test
    public void testLegalTransitions() {
        assertTrue(stateMachine.canTransition(ApplicationState.DISCOVERED, ApplicationState.QUALIFIED));
        assertTrue(stateMachine.canTransition(ApplicationState.QUALIFIED, ApplicationState.PREPARING));
        assertTrue(stateMachine.canTransition(ApplicationState.PREPARING, ApplicationState.READY_FOR_REVIEW));
        assertTrue(stateMachine.canTransition(ApplicationState.READY_FOR_REVIEW, ApplicationState.APPROVED));
        assertTrue(stateMachine.canTransition(ApplicationState.APPROVED, ApplicationState.APPLYING));
        assertTrue(stateMachine.canTransition(ApplicationState.APPLYING, ApplicationState.APPLIED));
        assertTrue(stateMachine.canTransition(ApplicationState.APPLIED, ApplicationState.INTERVIEW));
        assertTrue(stateMachine.canTransition(ApplicationState.INTERVIEW, ApplicationState.OFFER));
        assertTrue(stateMachine.canTransition(ApplicationState.OFFER, ApplicationState.CLOSED));
    }

    @Test
    public void testIllegalTransitions() {
        assertFalse(stateMachine.canTransition(ApplicationState.DISCOVERED, ApplicationState.APPLIED));
        assertFalse(stateMachine.canTransition(ApplicationState.OFFER, ApplicationState.APPLYING));
        assertFalse(stateMachine.canTransition(ApplicationState.APPLIED, ApplicationState.QUALIFIED));
        assertFalse(stateMachine.canTransition(ApplicationState.APPROVED, ApplicationState.INTERVIEW));
    }

    @Test
    public void testValidateTransitionThrowsExceptionOnIllegal() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> stateMachine.validateTransition(ApplicationState.DISCOVERED, ApplicationState.APPLIED)
        );
        assertTrue(ex.getMessage().contains("Illegal application state transition"));
    }

    @Test
    public void testSameStateTransitionAllowed() {
        assertTrue(stateMachine.canTransition(ApplicationState.DISCOVERED, ApplicationState.DISCOVERED));
        assertDoesNotThrow(() -> stateMachine.validateTransition(ApplicationState.APPLIED, ApplicationState.APPLIED));
    }
}
