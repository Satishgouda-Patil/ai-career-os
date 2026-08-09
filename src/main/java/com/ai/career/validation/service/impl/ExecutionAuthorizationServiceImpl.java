package com.ai.career.validation.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationApproval;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.validation.model.ExecutionAuthorization;
import com.ai.career.validation.service.ExecutionAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionAuthorizationServiceImpl implements ExecutionAuthorizationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationApprovalRepository approvalRepository;
    private final ApplicationStateMachine stateMachine;

    @Override
    @Transactional
    public ExecutionAuthorization authorizeExecution(Long userId, Long applicationId, String note) {
        log.info("Authorizing execution for application ID: {} by user ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        if (application.getStatus() != ApplicationState.APPROVED) {
            if (stateMachine.canTransition(application.getStatus(), ApplicationState.APPROVED)) {
                application.setStatus(ApplicationState.APPROVED);
                applicationRepository.save(application);
            } else {
                throw new IllegalStateException("Application state [" + application.getStatus() + "] cannot transition to APPROVED for execution authorization");
            }
        }

        List<ApplicationApproval> existing = approvalRepository.findByApplicationIdOrderByApprovedAtDesc(applicationId);
        ApplicationApproval approval;
        if (!existing.isEmpty()) {
            approval = existing.get(0);
        } else {
            approval = ApplicationApproval.builder()
                .application(application)
                .action("EXECUTION_AUTHORIZATION")
                .approvedBy(application.getUser())
                .build();
        }

        approval.setAction("EXECUTION_AUTHORIZATION");
        approval.setReason(note != null ? note : "Explicit candidate execution authorization granted");
        approval.setApprovedBy(application.getUser());
        approvalRepository.save(approval);

        return ExecutionAuthorization.builder()
            .applicationId(applicationId)
            .authorizedByUserId(userId)
            .authorizedAt(LocalDateTime.now())
            .executionAuthorized(true)
            .note(approval.getReason())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionAuthorization checkAuthorization(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        List<ApplicationApproval> existing = approvalRepository.findByApplicationIdOrderByApprovedAtDesc(applicationId);
        boolean authorized = !existing.isEmpty() && "EXECUTION_AUTHORIZATION".equalsIgnoreCase(existing.get(0).getAction());

        return ExecutionAuthorization.builder()
            .applicationId(applicationId)
            .authorizedByUserId(authorized ? userId : null)
            .authorizedAt(authorized ? LocalDateTime.now() : null)
            .executionAuthorized(authorized)
            .note(authorized ? "Execution authorized" : "Execution not authorized")
            .build();
    }
}
