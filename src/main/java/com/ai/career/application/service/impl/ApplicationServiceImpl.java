package com.ai.career.application.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.entity.ApplicationStateHistory;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.domain.repository.ApplicationStateHistoryRepository;
import com.ai.career.application.dto.ApplicationHistoryResponse;
import com.ai.career.application.dto.ApplicationResponse;
import com.ai.career.application.dto.CreateApplicationRequest;
import com.ai.career.application.dto.TransitionStateRequest;
import com.ai.career.application.service.ApplicationService;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.coverletter.domain.entity.CoverLetter;
import com.ai.career.coverletter.domain.repository.CoverLetterRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.job.dto.JobDto;
import com.ai.career.resume.domain.entity.ResumeVersion;
import com.ai.career.resume.domain.repository.ResumeVersionRepository;
import com.ai.career.workspace.domain.entity.Workspace;
import com.ai.career.workspace.domain.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStateHistoryRepository stateHistoryRepository;
    private final ApplicationStateMachine stateMachine;

    private static final List<ApplicationState> ACTIVE_STATES = Arrays.stream(ApplicationState.values())
        .filter(ApplicationState::isActive)
        .collect(Collectors.toList());

    @Override
    @Transactional
    public ApplicationResponse createApplication(Long userId, CreateApplicationRequest request) {
        log.info("Creating new Application for User ID: {}, Job ID: {}", userId, request.getJobId());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Job job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + request.getJobId()));

        // Check active application deduplication for (user_id, job_id)
        applicationRepository.findActiveApplicationByUserIdAndJobId(userId, request.getJobId(), ACTIVE_STATES)
            .ifPresent(existing -> {
                throw new IllegalStateException("An active application already exists for User ID: " + userId + " and Job ID: " + request.getJobId() + " (Application ID: " + existing.getId() + ")");
            });

        Workspace workspace = request.getWorkspaceId() != null ? workspaceRepository.findById(request.getWorkspaceId()).orElse(null) : null;
        ResumeVersion resumeVersion = request.getResumeVersionId() != null ? resumeVersionRepository.findById(request.getResumeVersionId()).orElse(null) : null;
        CoverLetter coverLetter = request.getCoverLetterId() != null ? coverLetterRepository.findById(request.getCoverLetterId()).orElse(null) : null;

        Application application = Application.builder()
            .user(user)
            .job(job)
            .workspace(workspace)
            .resumeVersion(resumeVersion)
            .coverLetter(coverLetter)
            .status(ApplicationState.DISCOVERED)
            .applicationMethod(request.getApplicationMethod() != null ? request.getApplicationMethod() : "MANUAL")
            .matchScore(request.getMatchScore())
            .atsScore(request.getAtsScore())
            .recommendation(request.getRecommendation())
            .applicationUrl(request.getApplicationUrl() != null ? request.getApplicationUrl() : job.getUrl())
            .providerName(request.getProviderName() != null ? request.getProviderName() : "DIRECT")
            .automationLevel(request.getAutomationLevel() != null ? request.getAutomationLevel() : "LEVEL_1")
            .approvalRequired(true)
            .build();

        Application saved = applicationRepository.save(application);

        // Record initial state history
        ApplicationStateHistory history = ApplicationStateHistory.builder()
            .application(saved)
            .fromStatus(null)
            .toStatus(ApplicationState.DISCOVERED)
            .reason("Application initialized")
            .triggerType("SYSTEM")
            .actorType("USER")
            .actorId(userId)
            .build();
        stateHistoryRepository.save(history);

        if (workspace != null) {
            workspace.setApplication(saved);
            workspaceRepository.save(workspace);
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ApplicationResponse transitionState(Long userId, Long applicationId, TransitionStateRequest request) {
        log.info("Transitioning Application ID: {} for User ID: {} to State: {}", applicationId, userId, request.getTargetState());

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        ApplicationState current = application.getStatus();
        ApplicationState target = request.getTargetState();

        // Validate state transition through domain state machine
        stateMachine.validateTransition(current, target);

        application.setStatus(target);
        LocalDateTime now = LocalDateTime.now();

        if (target == ApplicationState.APPLYING && application.getStartedAt() == null) {
            application.setStartedAt(now);
        } else if (target == ApplicationState.APPLIED && application.getSubmittedAt() == null) {
            application.setSubmittedAt(now);
            application.setVerifiedAt(now);
        } else if (!target.isActive() && application.getClosedAt() == null) {
            application.setClosedAt(now);
        }

        Application saved = applicationRepository.save(application);

        // Record state transition history log
        ApplicationStateHistory history = ApplicationStateHistory.builder()
            .application(saved)
            .fromStatus(current)
            .toStatus(target)
            .reason(request.getReason() != null ? request.getReason() : "State transitioned to " + target)
            .triggerType(request.getTriggerType() != null ? request.getTriggerType() : "USER")
            .actorType("USER")
            .actorId(userId)
            .correlationId(request.getCorrelationId())
            .build();
        stateHistoryRepository.save(history);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));
        return mapToDto(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(Long userId, ApplicationState status) {
        List<Application> apps = (status != null)
            ? applicationRepository.findByUserIdAndStatus(userId, status)
            : applicationRepository.findByUserId(userId);
        return apps.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationHistoryResponse> getApplicationHistory(Long userId, Long applicationId) {
        return stateHistoryRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
            .stream()
            .map(this::mapHistoryToDto)
            .collect(Collectors.toList());
    }

    private ApplicationResponse mapToDto(Application entity) {
        Job job = entity.getJob();
        JobDto jobDto = JobDto.builder()
            .id(job.getId())
            .source(job.getSource())
            .sourceJobId(job.getSourceJobId())
            .title(job.getTitle())
            .company(job.getCompany())
            .location(job.getLocation())
            .description(job.getDescription())
            .url(job.getUrl())
            .postedAt(job.getPostedAt())
            .build();

        List<ApplicationHistoryResponse> history = stateHistoryRepository
            .findByApplicationIdOrderByCreatedAtDesc(entity.getId())
            .stream()
            .map(this::mapHistoryToDto)
            .collect(Collectors.toList());

        return ApplicationResponse.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .jobId(job.getId())
            .job(jobDto)
            .workspaceId(entity.getWorkspace() != null ? entity.getWorkspace().getId() : null)
            .resumeVersionId(entity.getResumeVersion() != null ? entity.getResumeVersion().getId() : null)
            .coverLetterId(entity.getCoverLetter() != null ? entity.getCoverLetter().getId() : null)
            .status(entity.getStatus())
            .applicationMethod(entity.getApplicationMethod())
            .matchScore(entity.getMatchScore())
            .atsScore(entity.getAtsScore())
            .recommendation(entity.getRecommendation())
            .applicationUrl(entity.getApplicationUrl())
            .providerName(entity.getProviderName())
            .providerApplicationId(entity.getProviderApplicationId())
            .automationLevel(entity.getAutomationLevel())
            .approvalRequired(entity.isApprovalRequired())
            .startedAt(entity.getStartedAt())
            .submittedAt(entity.getSubmittedAt())
            .verifiedAt(entity.getVerifiedAt())
            .closedAt(entity.getClosedAt())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .history(history)
            .build();
    }

    private ApplicationHistoryResponse mapHistoryToDto(ApplicationStateHistory entity) {
        return ApplicationHistoryResponse.builder()
            .id(entity.getId())
            .applicationId(entity.getApplication().getId())
            .fromStatus(entity.getFromStatus())
            .toStatus(entity.getToStatus())
            .reason(entity.getReason())
            .triggerType(entity.getTriggerType())
            .actorType(entity.getActorType())
            .actorId(entity.getActorId())
            .correlationId(entity.getCorrelationId())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
