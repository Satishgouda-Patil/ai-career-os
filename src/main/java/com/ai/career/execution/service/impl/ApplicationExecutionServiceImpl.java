package com.ai.career.execution.service.impl;

import com.ai.career.application.domain.entity.*;
import com.ai.career.application.domain.repository.ApplicationExecutionRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.domain.repository.ApplicationStateHistoryRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.dto.ExecutionResponse;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.lock.ExecutionLockProperties;
import com.ai.career.execution.provider.*;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.execution.service.ApplicationExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationExecutionServiceImpl implements ApplicationExecutionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationExecutionRepository executionRepository;
    private final ApplicationStateHistoryRepository stateHistoryRepository;
    private final ApplicationExecutionProviderRegistry providerRegistry;
    private final ApplicationStateMachine stateMachine;
    private final DistributedExecutionLock distributedExecutionLock;
    private final ExecutionLockProperties lockProperties;

    @Override
    @Transactional
    public ExecutionResponse executeApplication(Long userId, Long applicationId, ExecuteApplicationRequest request) {
        log.info("Initiating execution for Application ID: {}, User ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        // 1. First Idempotency Check prior to lock acquisition
        checkActiveExecution(applicationId);

        // 2. Acquire Distributed Execution Lock (Fail Closed Safety)
        String lockKey = "application-execution:" + applicationId;
        String ownerId = UUID.randomUUID().toString();
        long leaseSeconds = lockProperties.getLeaseSeconds();

        boolean locked = distributedExecutionLock.acquire(lockKey, ownerId, leaseSeconds);
        if (!locked) {
            log.warn("Execution lock acquisition FAILED for Application ID: {}, OwnerId: [{}]. Aborting execution.", applicationId, ownerId);
            throw new IllegalStateException("LOCK_NOT_ACQUIRED: Execution lock is already held for Application ID: " + applicationId);
        }

        try {
            // 3. Mandatory Second Idempotency Check AFTER lock acquisition (Double-Check Safeguard)
            checkActiveExecution(applicationId);

            // Transition application state to APPLYING if approved
            ApplicationState currentState = application.getStatus();
            if (currentState == ApplicationState.APPROVED) {
                stateMachine.validateTransition(currentState, ApplicationState.APPLYING);
                application.setStatus(ApplicationState.APPLYING);
                if (application.getStartedAt() == null) {
                    application.setStartedAt(LocalDateTime.now());
                }
                applicationRepository.save(application);

                stateHistoryRepository.save(ApplicationStateHistory.builder()
                    .application(application)
                    .fromStatus(currentState)
                    .toStatus(ApplicationState.APPLYING)
                    .reason("Execution initiated by user")
                    .triggerType("USER")
                    .actorType("USER")
                    .actorId(userId)
                    .build());
            } else if (currentState != ApplicationState.APPLYING) {
                throw new IllegalStateException("Application must be in APPROVED or APPLYING state before execution (Current: " + currentState + ")");
            }

            // Resolve Provider
            ApplicationExecutionProvider provider = providerRegistry.resolve(application);
            boolean dryRun = request != null && request.isDryRun();

            ExecutionContext context = ExecutionContext.builder()
                .applicationId(application.getId())
                .userId(userId)
                .jobId(application.getJob().getId())
                .providerName(provider.getProviderName())
                .jobUrl(application.getApplicationUrl())
                .resumeUrl(application.getResumeVersion() != null ? application.getResumeVersion().getPdfUrl() : null)
                .coverLetterContent(application.getCoverLetter() != null ? application.getCoverLetter().getContent() : null)
                .applicationAnswers(request != null ? request.getAnswers() : null)
                .automationLevel(application.getAutomationLevel())
                .dryRun(dryRun)
                .correlationId(UUID.randomUUID().toString())
                .build();

            // Create running ApplicationExecution record
            ApplicationExecution execution = ApplicationExecution.builder()
                .application(application)
                .providerName(provider.getProviderName())
                .status(ApplicationExecutionStatus.RUNNING)
                .executionLogs("Execution session started with provider: " + provider.getProviderName())
                .startedAt(LocalDateTime.now())
                .build();

            execution = executionRepository.save(execution);
            context.setExecutionId(execution.getId());

            // Validate context prior to execution
            ExecutionValidationResult validation = provider.validate(application, context);
            if (!validation.isValid()) {
                execution.setStatus(ApplicationExecutionStatus.FAILED);
                execution.setOutcomeStatus(ExecutionOutcomeStatus.FAILED.name());
                execution.setErrorMessage(validation.getErrorMessage());
                execution.setCompletedAt(LocalDateTime.now());
                executionRepository.save(execution);

                // Revert application state to ACTION_REQUIRED
                transitionApplicationState(application, ApplicationState.APPLYING, ApplicationState.ACTION_REQUIRED, "Validation failed: " + validation.getErrorMessage(), userId);

                return mapExecutionToDto(execution, application.getStatus());
            }

            // Invoke Provider Execution
            ExecutionResult result = provider.execute(application, context);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setOutcomeStatus(result.getStatus().name());
            execution.setExternalApplicationId(result.getExternalApplicationId());
            execution.setExternalUrl(result.getExternalUrl());
            execution.setErrorCode(result.getErrorCode());
            execution.setErrorMessage(result.getErrorMessage());
            execution.setRetryable(result.isRetryable());
            execution.setExecutionLogs(execution.getExecutionLogs() + "\nProvider outcome: " + result.getStatus() + (result.getErrorMessage() != null ? " - " + result.getErrorMessage() : ""));

            // Determine ApplicationState transition based strictly on verified provider result
            ApplicationState previousState = application.getStatus();
            ApplicationState nextState;

            switch (result.getStatus()) {
                case SUCCESS:
                    execution.setStatus(ApplicationExecutionStatus.SUCCEEDED);
                    nextState = ApplicationState.APPLIED;
                    if (result.getExternalApplicationId() != null) {
                        application.setProviderApplicationId(result.getExternalApplicationId());
                    }
                    break;

                case REQUIRES_HUMAN:
                    execution.setStatus(ApplicationExecutionStatus.ACTION_REQUIRED);
                    nextState = ApplicationState.ACTION_REQUIRED;
                    break;

                case FAILED:
                case UNSUPPORTED:
                    execution.setStatus(ApplicationExecutionStatus.FAILED);
                    nextState = ApplicationState.FAILED;
                    break;

                case UNKNOWN:
                default:
                    execution.setStatus(ApplicationExecutionStatus.UNKNOWN);
                    nextState = ApplicationState.SUBMISSION_REQUIRES_REVIEW; // Safety rule: UNKNOWN never automatically transitions to APPLIED
                    break;
            }

            executionRepository.save(execution);
            transitionApplicationState(application, previousState, nextState, "Provider execution outcome: " + result.getStatus(), userId);

            return mapExecutionToDto(execution, application.getStatus());

        } finally {
            // 4. Safe Release of Lock (Owner Identity Verified via Lua Script)
            distributedExecutionLock.release(lockKey, ownerId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionValidationResult validateExecution(Long userId, Long applicationId, ExecuteApplicationRequest request) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        ApplicationExecutionProvider provider = providerRegistry.resolve(application);

        ExecutionContext context = ExecutionContext.builder()
            .applicationId(application.getId())
            .userId(userId)
            .jobId(application.getJob().getId())
            .providerName(provider.getProviderName())
            .jobUrl(application.getApplicationUrl())
            .applicationAnswers(request != null ? request.getAnswers() : null)
            .dryRun(true)
            .build();

        return provider.validate(application, context);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> getExecutionsByApplicationId(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        return executionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
            .stream()
            .map(exec -> mapExecutionToDto(exec, application.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionResponse getExecutionById(Long userId, Long applicationId, Long executionId) {
        ApplicationExecution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new IllegalArgumentException("Execution not found with ID: " + executionId));

        Application application = execution.getApplication();
        if (!application.getUser().getId().equals(userId) || !application.getId().equals(applicationId)) {
            throw new IllegalArgumentException("Unauthorized access to execution ID: " + executionId);
        }

        return mapExecutionToDto(execution, application.getStatus());
    }

    private void checkActiveExecution(Long applicationId) {
        List<ApplicationExecution> existingExecutions = executionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        boolean hasActiveExecution = existingExecutions.stream()
            .anyMatch(exec -> exec.getStatus() == ApplicationExecutionStatus.PENDING ||
                             exec.getStatus() == ApplicationExecutionStatus.VALIDATING ||
                             exec.getStatus() == ApplicationExecutionStatus.RUNNING);

        if (hasActiveExecution) {
            throw new IllegalStateException("An active execution is already running for Application ID: " + applicationId);
        }
    }

    private void transitionApplicationState(Application application, ApplicationState from, ApplicationState to, String reason, Long userId) {
        if (from != to && stateMachine.canTransition(from, to)) {
            application.setStatus(to);
            if (to == ApplicationState.APPLIED && application.getSubmittedAt() == null) {
                application.setSubmittedAt(LocalDateTime.now());
                application.setVerifiedAt(LocalDateTime.now());
            } else if (!to.isActive() && application.getClosedAt() == null) {
                application.setClosedAt(LocalDateTime.now());
            }

            applicationRepository.save(application);

            stateHistoryRepository.save(ApplicationStateHistory.builder()
                .application(application)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .triggerType("SYSTEM")
                .actorType("SYSTEM")
                .actorId(userId)
                .build());
        }
    }

    private ExecutionResponse mapExecutionToDto(ApplicationExecution entity, ApplicationState applicationStatus) {
        ExecutionOutcomeStatus outcome = null;
        if (entity.getOutcomeStatus() != null) {
            try {
                outcome = ExecutionOutcomeStatus.valueOf(entity.getOutcomeStatus());
            } catch (Exception ignored) {
            }
        }

        return ExecutionResponse.builder()
            .id(entity.getId())
            .applicationId(entity.getApplication().getId())
            .providerName(entity.getProviderName())
            .executionStatus(entity.getStatus())
            .outcomeStatus(outcome)
            .applicationStatus(applicationStatus)
            .externalApplicationId(entity.getExternalApplicationId())
            .externalUrl(entity.getExternalUrl())
            .errorCode(entity.getErrorCode())
            .errorMessage(entity.getErrorMessage())
            .retryable(entity.isRetryable())
            .executionLogs(entity.getExecutionLogs())
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
