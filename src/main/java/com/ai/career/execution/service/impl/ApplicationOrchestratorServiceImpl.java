package com.ai.career.execution.service.impl;

import com.ai.career.application.domain.entity.*;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.domain.repository.ApplicationStateHistoryRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.domain.entity.ApplicationWorkflowRun;
import com.ai.career.execution.domain.repository.ApplicationWorkflowRunRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.WorkflowOrchestrationResponse;
import com.ai.career.execution.event.*;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.lock.ExecutionLockProperties;
import com.ai.career.execution.service.ApplicationOrchestratorService;
import com.ai.career.execution.service.ApplicationReadinessEvaluator;
import com.ai.career.execution.service.ApplicationReadinessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationOrchestratorServiceImpl implements ApplicationOrchestratorService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationApprovalRepository approvalRepository;
    private final ApplicationStateHistoryRepository stateHistoryRepository;
    private final ApplicationWorkflowRunRepository workflowRunRepository;
    private final UserRepository userRepository;
    private final ApplicationReadinessEvaluator readinessEvaluator;
    private final ApplicationStateMachine stateMachine;
    private final DistributedExecutionLock distributedExecutionLock;
    private final ExecutionLockProperties lockProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public WorkflowOrchestrationResponse orchestrate(Long userId, Long applicationId, ExecuteApplicationRequest request) {
        log.info("Initiating orchestration pipeline for Application ID: {}, User ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        String workflowType = "STANDARD_APPLICATION_WORKFLOW";
        String approvalVersion = "v1";
        String idempotencyKey = applicationId + "_" + workflowType + "_" + approvalVersion;
        String correlationId = UUID.randomUUID().toString();

        // 1. Idempotency Check
        ApplicationWorkflowRun existingRun = workflowRunRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingRun != null && "SUCCESS".equalsIgnoreCase(existingRun.getStatus())) {
            log.info("Idempotent orchestration request for Application ID: {} - returning existing workflow run ID {}", applicationId, existingRun.getId());
            return mapToResponse(existingRun, readinessEvaluator.evaluate(userId, applicationId));
        }

        // 2. Redis Distributed Lock
        String lockKey = "application-execution:" + applicationId;
        String ownerId = UUID.randomUUID().toString();
        boolean locked = distributedExecutionLock.acquire(lockKey, ownerId, lockProperties.getLeaseSeconds());
        if (!locked) {
            log.warn("Distributed execution lock acquisition FAILED for Application ID: {}", applicationId);
            throw new IllegalStateException("LOCK_NOT_ACQUIRED: Execution lock is already held for Application ID: " + applicationId);
        }

        ApplicationWorkflowRun workflowRun = ApplicationWorkflowRun.builder()
                .application(application)
                .workflowType(workflowType)
                .status("RUNNING")
                .idempotencyKey(idempotencyKey)
                .correlationId(correlationId)
                .currentStage("LOAD_APPLICATION")
                .startedAt(LocalDateTime.now())
                .build();

        workflowRun = workflowRunRepository.save(workflowRun);

        try {
            // Stage: EVALUATE_READINESS
            workflowRun.setCurrentStage("EVALUATE_READINESS");
            workflowRunRepository.save(workflowRun);

            ApplicationReadinessResult readiness = readinessEvaluator.evaluate(userId, applicationId);
            if (!readiness.isReady()) {
                String failureCode = !readiness.getUnresolvedFields().isEmpty() ? "UNRESOLVED_FIELDS"
                        : (!readiness.getMissingArtifacts().isEmpty() ? "MISSING_ARTIFACTS" : "HUMAN_REVIEW_REQUIRED");

                workflowRun.setStatus("SUBMISSION_REQUIRES_REVIEW");
                workflowRun.setFailureCode(failureCode);
                workflowRun.setCurrentStage("STOPPED_HUMAN_REVIEW");
                workflowRun.setCompletedAt(LocalDateTime.now());
                workflowRunRepository.save(workflowRun);

                // Transition state safety rule
                transitionState(application, ApplicationState.SUBMISSION_REQUIRES_REVIEW, "Orchestration stopped for human review: " + failureCode, userId);

                eventPublisher.publishEvent(ApplicationExecutionRequiresReviewEvent.builder()
                        .applicationId(applicationId)
                        .userId(userId)
                        .correlationId(correlationId)
                        .timestamp(LocalDateTime.now())
                        .reason(failureCode)
                        .reviewReasons(readiness.getReviewReasons())
                        .build());

                return mapToResponse(workflowRun, readiness);
            }

            // Stage: VALIDATE_APPROVAL & REQUEST_EXECUTION
            workflowRun.setCurrentStage("VALIDATE_APPROVAL");
            workflowRunRepository.save(workflowRun);

            if (application.getStatus() == ApplicationState.APPROVED) {
                eventPublisher.publishEvent(ApplicationApprovedEvent.builder()
                        .applicationId(applicationId)
                        .userId(userId)
                        .correlationId(correlationId)
                        .timestamp(LocalDateTime.now())
                        .approvedBy(String.valueOf(userId))
                        .build());
            }

            // Stage: REQUEST_EXECUTION
            workflowRun.setCurrentStage("REQUEST_EXECUTION");
            transitionState(application, ApplicationState.APPLYING, "Execution requested by orchestrator", userId);

            eventPublisher.publishEvent(ApplicationExecutionRequestedEvent.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .providerName(application.getProviderName() != null ? application.getProviderName() : "GENERIC_JOB_FORM")
                    .idempotencyKey(idempotencyKey)
                    .build());

            eventPublisher.publishEvent(ApplicationExecutionStartedEvent.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .providerName(application.getProviderName() != null ? application.getProviderName() : "GENERIC_JOB_FORM")
                    .build());

            workflowRun.setStatus("SUCCESS");
            workflowRun.setCurrentStage("EXECUTION_REQUESTED");
            workflowRun.setCompletedAt(LocalDateTime.now());
            workflowRunRepository.save(workflowRun);

            return mapToResponse(workflowRun, readiness);

        } catch (Exception e) {
            log.error("Orchestration pipeline encountered failure for Application ID: {}", applicationId, e);
            workflowRun.setStatus("FAILED");
            workflowRun.setFailureCode(e.getMessage());
            workflowRun.setCompletedAt(LocalDateTime.now());
            workflowRunRepository.save(workflowRun);

            eventPublisher.publishEvent(ApplicationExecutionFailedEvent.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .correlationId(correlationId)
                    .timestamp(LocalDateTime.now())
                    .errorCode("ORCHESTRATION_ERROR")
                    .errorMessage(e.getMessage())
                    .build());

            throw e;
        } finally {
            distributedExecutionLock.release(lockKey, ownerId);
        }
    }

    @Override
    @Transactional
    public WorkflowOrchestrationResponse approveAndPrepare(Long userId, Long applicationId, String approvedBy, String reason) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Record Approval
        ApplicationApproval approval = ApplicationApproval.builder()
                .application(application)
                .action("APPROVE")
                .reason(reason != null ? reason : "Explicit human approval granted")
                .approvedBy(user)
                .approvedAt(LocalDateTime.now())
                .build();
        approvalRepository.save(approval);

        transitionState(application, ApplicationState.APPROVED, "Approved by human user: " + approvedBy, userId);

        eventPublisher.publishEvent(ApplicationApprovedEvent.builder()
                .applicationId(applicationId)
                .userId(userId)
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .approvedBy(approvedBy)
                .build());

        return orchestrate(userId, applicationId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationReadinessResult getReadiness(Long userId, Long applicationId) {
        return readinessEvaluator.evaluate(userId, applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowOrchestrationResponse> getWorkflowHistory(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        ApplicationReadinessResult readiness = readinessEvaluator.evaluate(userId, applicationId);
        return workflowRunRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .map(run -> mapToResponse(run, readiness))
                .collect(Collectors.toList());
    }

    private void transitionState(Application application, ApplicationState targetState, String reason, Long userId) {
        ApplicationState currentState = application.getStatus();
        if (currentState != targetState) {
            stateMachine.validateTransition(currentState, targetState);
            application.setStatus(targetState);
            applicationRepository.save(application);

            stateHistoryRepository.save(ApplicationStateHistory.builder()
                    .application(application)
                    .fromStatus(currentState)
                    .toStatus(targetState)
                    .reason(reason)
                    .triggerType("SYSTEM")
                    .actorType("USER")
                    .actorId(userId)
                    .build());
        }
    }

    private WorkflowOrchestrationResponse mapToResponse(ApplicationWorkflowRun run, ApplicationReadinessResult readiness) {
        return WorkflowOrchestrationResponse.builder()
                .workflowRunId(run.getId())
                .applicationId(run.getApplication().getId())
                .workflowType(run.getWorkflowType())
                .status(run.getStatus())
                .idempotencyKey(run.getIdempotencyKey())
                .correlationId(run.getCorrelationId())
                .currentStage(run.getCurrentStage())
                .failureCode(run.getFailureCode())
                .retryCount(run.getRetryCount())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .readiness(readiness)
                .build();
    }
}
