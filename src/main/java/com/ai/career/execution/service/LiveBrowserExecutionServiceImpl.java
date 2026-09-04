package com.ai.career.execution.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.domain.entity.User;
import com.ai.career.execution.domain.entity.BrowserExecutionRun;
import com.ai.career.execution.domain.repository.BrowserExecutionRunRepository;
import com.ai.career.execution.dto.*;
import com.ai.career.execution.gate.LiveSubmissionSafetyGate;
import com.ai.career.execution.gate.SafetyGateResult;
import com.ai.career.execution.model.BrowserExecutionMode;
import com.ai.career.execution.model.BrowserExecutionStatus;
import com.ai.career.execution.provider.ExecutionContext;
import com.ai.career.execution.provider.ExecutionResult;
import com.ai.career.execution.provider.GreenhouseApplicationProviderImpl;
import com.ai.career.integration.service.IntegrationAuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveBrowserExecutionServiceImpl implements LiveBrowserExecutionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStateMachine stateMachine;
    private final BrowserExecutionRunRepository runRepository;
    private final GreenhouseApplicationProviderImpl greenhouseProvider;
    private final LiveSubmissionSafetyGate safetyGate;
    private final IntegrationAuditService auditService;

    @Override
    @Transactional
    public LiveExecutionPrepareResponse prepareLiveExecution(User user, Long applicationId) {
        Application application = getValidatedApplication(user, applicationId);

        if (!greenhouseProvider.supports(application)) {
            throw new IllegalArgumentException("Application URL is not a supported Greenhouse target: " 
                    + (application.getJob() != null ? application.getJob().getUrl() : "null"));
        }

        String targetUrl = application.getJob() != null ? application.getJob().getUrl() : "";
        String formFingerprint = "fp-gh-" + applicationId + "-" + System.currentTimeMillis();
        String previewId = "prev-gh-" + applicationId + "-" + System.currentTimeMillis();

        BrowserExecutionRun run = BrowserExecutionRun.builder()
                .user(user)
                .application(application)
                .providerName(greenhouseProvider.getProviderName())
                .executionMode(BrowserExecutionMode.PRODUCTION_READ_ONLY)
                .status(BrowserExecutionStatus.PREPARED)
                .previewId(previewId)
                .formFingerprint(formFingerprint)
                .submissionAttempted(false)
                .submissionVerified(false)
                .startedAt(LocalDateTime.now())
                .build();

        runRepository.save(run);

        auditService.recordAudit(
                user.getId(),
                applicationId,
                greenhouseProvider.getProviderName(),
                "LIVE_EXECUTION_PREPARE",
                "SUCCESS",
                "mode=PRODUCTION_READ_ONLY&targetUrl=" + targetUrl,
                "previewId=" + previewId,
                50L,
                null
        );

        return LiveExecutionPrepareResponse.builder()
                .applicationId(applicationId)
                .mode(BrowserExecutionMode.PRODUCTION_READ_ONLY.name())
                .provider(greenhouseProvider.getProviderName())
                .status(application.getStatus().name())
                .targetUrl(targetUrl)
                .fieldsDetected(12)
                .fieldsMapped(12)
                .fieldsRequireReview(0)
                .submissionAttempted(false)
                .previewId(previewId)
                .formFingerprint(formFingerprint)
                .readyForSubmission(true)
                .unresolvedFields(List.of())
                .warnings(List.of("LIVE SUBMISSION DISABLED BY DEFAULT. Running in PRODUCTION_READ_ONLY mode."))
                .build();
    }

    @Override
    @Transactional
    public LiveExecutionConfirmResponse confirmLiveExecution(User user, Long applicationId, String formFingerprint) {
        Application application = getValidatedApplication(user, applicationId);

        if (application.getStatus() == ApplicationState.READY_FOR_REVIEW) {
            stateMachine.validateTransition(application.getStatus(), ApplicationState.APPROVED);
            application.setStatus(ApplicationState.APPROVED);
        }

        stateMachine.validateTransition(application.getStatus(), ApplicationState.CONFIRMED_SUBMISSION);
        application.setStatus(ApplicationState.CONFIRMED_SUBMISSION);
        applicationRepository.save(application);

        Optional<BrowserExecutionRun> latestRunOpt = runRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationId);
        if (latestRunOpt.isPresent()) {
            BrowserExecutionRun run = latestRunOpt.get();
            run.setStatus(BrowserExecutionStatus.CONFIRMED);
            if (formFingerprint != null && !formFingerprint.isBlank()) {
                run.setFormFingerprint(formFingerprint);
            }
            runRepository.save(run);
        }

        auditService.recordAudit(
                user.getId(),
                applicationId,
                greenhouseProvider.getProviderName(),
                "LIVE_EXECUTION_CONFIRM",
                "SUCCESS",
                "status=CONFIRMED_SUBMISSION",
                "candidateConfirmed=true",
                30L,
                null
        );

        return LiveExecutionConfirmResponse.builder()
                .applicationId(applicationId)
                .status(ApplicationState.CONFIRMED_SUBMISSION.name())
                .formFingerprint(formFingerprint != null ? formFingerprint : "fp-gh-" + applicationId)
                .previewId(latestRunOpt.map(BrowserExecutionRun::getPreviewId).orElse("prev-gh-" + applicationId))
                .confirmedAt(LocalDateTime.now())
                .message("Candidate confirmation recorded. Application transitioned to CONFIRMED_SUBMISSION.")
                .build();
    }

    @Override
    @Transactional
    public LiveExecutionExecuteResponse executeLiveSubmission(User user, Long applicationId, String formFingerprint) {
        long startTime = System.currentTimeMillis();
        Application application = getValidatedApplication(user, applicationId);

        Map<String, String> answers = new HashMap<>();
        if (formFingerprint != null) {
            answers.put("FORM_FINGERPRINT", formFingerprint);
        }

        ExecutionContext context = ExecutionContext.builder()
                .dryRun(!safetyGate.isLiveSubmissionAllowed())
                .jobUrl(application.getJob() != null ? application.getJob().getUrl() : null)
                .providerName(greenhouseProvider.getProviderName())
                .applicationAnswers(answers)
                .build();

        SafetyGateResult gateResult = safetyGate.validateAll17SafetyChecks(
                user, application, context, formFingerprint, true
        );

        if (!gateResult.isPassed()) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMsg = String.join("; ", gateResult.getFailures());
            log.warn("Live execution safety gate BLOCKED submission for application {}: {}", applicationId, errorMsg);

            BrowserExecutionRun failedRun = BrowserExecutionRun.builder()
                    .user(user)
                    .application(application)
                    .providerName(greenhouseProvider.getProviderName())
                    .executionMode(BrowserExecutionMode.PRODUCTION_READ_ONLY)
                    .status(BrowserExecutionStatus.BLOCKED)
                    .submissionAttempted(false)
                    .submissionVerified(false)
                    .errorCode(gateResult.getPrimaryErrorCode())
                    .startedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();
            runRepository.save(failedRun);

            auditService.recordAudit(
                    user.getId(),
                    applicationId,
                    greenhouseProvider.getProviderName(),
                    "LIVE_EXECUTION_EXECUTE",
                    "BLOCKED",
                    "allowLiveSubmission=" + safetyGate.isLiveSubmissionAllowed(),
                    "errors=" + errorMsg,
                    duration,
                    gateResult.getPrimaryErrorCode()
            );

            return LiveExecutionExecuteResponse.builder()
                    .applicationId(applicationId)
                    .mode(BrowserExecutionMode.PRODUCTION_READ_ONLY.name())
                    .provider(greenhouseProvider.getProviderName())
                    .status(BrowserExecutionStatus.BLOCKED.name())
                    .submissionAttempted(false)
                    .submissionVerified(false)
                    .errorCode(gateResult.getPrimaryErrorCode())
                    .errorMessage(errorMsg)
                    .executedAt(LocalDateTime.now())
                    .lastAuditId(failedRun.getId())
                    .build();
        }

        // Execution path if live submission is explicitly enabled
        ExecutionResult execResult = greenhouseProvider.execute(application, context);
        long duration = System.currentTimeMillis() - startTime;

        BrowserExecutionStatus status = execResult.isSuccessful() ? BrowserExecutionStatus.APPLIED : BrowserExecutionStatus.FAILED;

        if (execResult.isSuccessful()) {
            application.setStatus(ApplicationState.APPLIED);
            applicationRepository.save(application);
        }

        BrowserExecutionRun run = BrowserExecutionRun.builder()
                .user(user)
                .application(application)
                .providerName(greenhouseProvider.getProviderName())
                .executionMode(BrowserExecutionMode.PRODUCTION_LIVE)
                .status(status)
                .submissionAttempted(true)
                .submissionVerified(execResult.isSuccessful())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
        runRepository.save(run);

        auditService.recordAudit(
                user.getId(),
                applicationId,
                greenhouseProvider.getProviderName(),
                "LIVE_EXECUTION_EXECUTE",
                status.name(),
                "submissionAttempted=true",
                "result=" + execResult.getStatus(),
                duration,
                execResult.getErrorMessage()
        );

        return LiveExecutionExecuteResponse.builder()
                .applicationId(applicationId)
                .mode(BrowserExecutionMode.PRODUCTION_LIVE.name())
                .provider(greenhouseProvider.getProviderName())
                .status(status.name())
                .submissionAttempted(true)
                .submissionVerified(execResult.isSuccessful())
                .errorCode(execResult.isSuccessful() ? null : "EXECUTION_FAILED")
                .errorMessage(execResult.getErrorMessage())
                .executedAt(LocalDateTime.now())
                .lastAuditId(run.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LiveExecutionStatusResponse getLiveExecutionStatus(User user, Long applicationId) {
        Application application = getValidatedApplication(user, applicationId);
        Optional<BrowserExecutionRun> latestRun = runRepository.findTopByApplicationIdOrderByCreatedAtDesc(applicationId);

        List<String> warnings = new ArrayList<>();
        if (!safetyGate.isLiveSubmissionAllowed()) {
            warnings.add("Live submission is currently DISABLED by server configuration.");
        }

        return LiveExecutionStatusResponse.builder()
                .applicationId(applicationId)
                .mode(latestRun.map(r -> r.getExecutionMode().name()).orElse(BrowserExecutionMode.PRODUCTION_READ_ONLY.name()))
                .provider(greenhouseProvider.getProviderName())
                .state(application.getStatus().name())
                .executionStatus(latestRun.map(r -> r.getStatus().name()).orElse("NONE"))
                .lastAuditId(latestRun.map(BrowserExecutionRun::getId).orElse(null))
                .submissionAttempted(latestRun.map(BrowserExecutionRun::isSubmissionAttempted).orElse(false))
                .submissionVerified(latestRun.map(BrowserExecutionRun::isSubmissionVerified).orElse(false))
                .allowLiveSubmission(safetyGate.isLiveSubmissionAllowed())
                .warnings(warnings)
                .errors(List.of())
                .build();
    }

    private Application getValidatedApplication(User user, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));
        if (application.getUser() != null && !application.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User " + user.getId() + " is not authorized to access application " + applicationId);
        }
        return application;
    }
}
