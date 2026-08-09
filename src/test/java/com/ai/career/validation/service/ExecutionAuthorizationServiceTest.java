package com.ai.career.validation.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationApproval;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.domain.entity.User;
import com.ai.career.validation.model.ExecutionAuthorization;
import com.ai.career.validation.service.impl.ExecutionAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExecutionAuthorizationServiceTest {

    private ApplicationRepository applicationRepository;
    private ApplicationApprovalRepository approvalRepository;
    private ApplicationStateMachine stateMachine;

    private ExecutionAuthorizationService authorizationService;

    @BeforeEach
    public void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        approvalRepository = mock(ApplicationApprovalRepository.class);
        stateMachine = mock(ApplicationStateMachine.class);

        authorizationService = new ExecutionAuthorizationServiceImpl(applicationRepository, approvalRepository, stateMachine);
    }

    @Test
    public void testExecutionAuthorizationUpdatesStateAndApprovalRecord() {
        User user = User.builder().id(5L).build();
        Application app = Application.builder().id(50L).user(user).status(ApplicationState.READY_FOR_REVIEW).build();

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(app));
        when(stateMachine.canTransition(ApplicationState.READY_FOR_REVIEW, ApplicationState.APPROVED)).thenReturn(true);
        when(approvalRepository.findByApplicationIdOrderByApprovedAtDesc(50L)).thenReturn(List.of());

        ExecutionAuthorization auth = authorizationService.authorizeExecution(5L, 50L, "Approved by candidate");

        assertNotNull(auth);
        assertEquals(50L, auth.getApplicationId());
        assertEquals(5L, auth.getAuthorizedByUserId());
        assertTrue(auth.isExecutionAuthorized());
        assertEquals("Approved by candidate", auth.getNote());

        assertEquals(ApplicationState.APPROVED, app.getStatus());
        verify(applicationRepository, times(1)).save(app);
        verify(approvalRepository, times(1)).save(any(ApplicationApproval.class));
    }

    @Test
    public void testAuthorizationFailsForNonOwner() {
        User owner = User.builder().id(5L).build();
        Application app = Application.builder().id(50L).user(owner).status(ApplicationState.READY_FOR_REVIEW).build();

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(app));

        assertThrows(IllegalArgumentException.class, () -> {
            authorizationService.authorizeExecution(99L, 50L, "Unauthorized");
        });
    }
}
