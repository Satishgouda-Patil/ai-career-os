package com.ai.career.execution.service;

import com.ai.career.application.domain.entity.*;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.domain.repository.ApplicationStateHistoryRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.domain.entity.ApplicationWorkflowRun;
import com.ai.career.execution.domain.repository.ApplicationWorkflowRunRepository;
import com.ai.career.execution.dto.WorkflowOrchestrationResponse;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.lock.ExecutionLockProperties;
import com.ai.career.execution.service.impl.ApplicationOrchestratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ApplicationOrchestratorServiceTest {

    private ApplicationRepository applicationRepository;
    private ApplicationApprovalRepository approvalRepository;
    private ApplicationStateHistoryRepository stateHistoryRepository;
    private ApplicationWorkflowRunRepository workflowRunRepository;
    private UserRepository userRepository;
    private ApplicationReadinessEvaluator readinessEvaluator;
    private ApplicationStateMachine stateMachine;
    private DistributedExecutionLock distributedExecutionLock;
    private ExecutionLockProperties lockProperties;
    private ApplicationEventPublisher eventPublisher;

    private ApplicationOrchestratorServiceImpl orchestratorService;

    @BeforeEach
    void setUp() {
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        approvalRepository = Mockito.mock(ApplicationApprovalRepository.class);
        stateHistoryRepository = Mockito.mock(ApplicationStateHistoryRepository.class);
        workflowRunRepository = Mockito.mock(ApplicationWorkflowRunRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        readinessEvaluator = Mockito.mock(ApplicationReadinessEvaluator.class);
        stateMachine = Mockito.mock(ApplicationStateMachine.class);
        distributedExecutionLock = Mockito.mock(DistributedExecutionLock.class);
        lockProperties = Mockito.mock(ExecutionLockProperties.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(lockProperties.getLeaseSeconds()).thenReturn(30L);
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        orchestratorService = new ApplicationOrchestratorServiceImpl(
                applicationRepository,
                approvalRepository,
                stateHistoryRepository,
                workflowRunRepository,
                userRepository,
                readinessEvaluator,
                stateMachine,
                distributedExecutionLock,
                lockProperties,
                eventPublisher
        );
    }

    @Test
    void testSuccessfulOrchestrationFlow() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Job job = Job.builder().id(10L).title("Full Stack Engineer").url("https://example.com/job/10").build();
        Application app = Application.builder().id(100L).user(user).job(job).status(ApplicationState.APPROVED).build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(workflowRunRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        ApplicationReadinessResult readiness = ApplicationReadinessResult.builder()
                .ready(true)
                .applicationId(100L)
                .currentState("APPROVED")
                .build();
        when(readinessEvaluator.evaluate(1L, 100L)).thenReturn(readiness);

        when(workflowRunRepository.save(any())).thenAnswer(invocation -> {
            ApplicationWorkflowRun run = invocation.getArgument(0);
            if (run.getId() == null) run.setId(1000L);
            return run;
        });

        WorkflowOrchestrationResponse response = orchestratorService.orchestrate(1L, 100L, null);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(100L, response.getApplicationId());
        assertEquals("EXECUTION_REQUESTED", response.getCurrentStage());
    }

    @Test
    void testOrchestrationStopsForHumanReviewWhenNotReady() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Job job = Job.builder().id(10L).title("Full Stack Engineer").url("https://example.com/job/10").build();
        Application app = Application.builder().id(100L).user(user).job(job).status(ApplicationState.READY_FOR_REVIEW).build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(workflowRunRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        ApplicationReadinessResult readiness = ApplicationReadinessResult.builder()
                .ready(false)
                .applicationId(100L)
                .currentState("READY_FOR_REVIEW")
                .unresolvedFields(java.util.List.of("workAuthorization"))
                .reviewReasons(java.util.List.of("Unresolved required field: workAuthorization"))
                .build();
        when(readinessEvaluator.evaluate(1L, 100L)).thenReturn(readiness);

        when(workflowRunRepository.save(any())).thenAnswer(invocation -> {
            ApplicationWorkflowRun run = invocation.getArgument(0);
            if (run.getId() == null) run.setId(1000L);
            return run;
        });

        WorkflowOrchestrationResponse response = orchestratorService.orchestrate(1L, 100L, null);

        assertNotNull(response);
        assertEquals("SUBMISSION_REQUIRES_REVIEW", response.getStatus());
        assertEquals("UNRESOLVED_FIELDS", response.getFailureCode());
    }
}
