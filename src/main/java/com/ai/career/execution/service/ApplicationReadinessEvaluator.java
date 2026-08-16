package com.ai.career.execution.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationApproval;
import com.ai.career.application.domain.entity.ApplicationExecution;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationApprovalRepository;
import com.ai.career.application.domain.repository.ApplicationExecutionRepository;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.form.entity.ApplicationFormPlanEntity;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.model.FieldAnswerMapping;
import com.ai.career.form.model.FormReadinessStatus;
import com.ai.career.form.repository.ApplicationFormPlanRepository;
import com.ai.career.workspace.domain.entity.Workspace;
import com.ai.career.workspace.domain.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReadinessEvaluator {

    private final ApplicationRepository applicationRepository;
    private final ApplicationApprovalRepository approvalRepository;
    private final ApplicationExecutionRepository executionRepository;
    private final ApplicationFormPlanRepository formPlanRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ObjectMapper objectMapper;

    public ApplicationReadinessResult evaluate(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        List<String> missingArtifacts = new ArrayList<>();
        List<String> unresolvedFields = new ArrayList<>();
        List<String> reviewReasons = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Validate State Eligibility
        ApplicationState state = application.getStatus();
        boolean stateEligible = (state == ApplicationState.APPROVED ||
                state == ApplicationState.READY_FOR_REVIEW ||
                state == ApplicationState.QUALIFIED ||
                state == ApplicationState.PREPARING ||
                state == ApplicationState.APPLYING);

        if (!stateEligible) {
            reviewReasons.add("Application state [" + state + "] is not eligible for execution");
        }

        // 2. Validate Workspace & Artifacts
        if (application.getJob() != null) {
            Workspace workspace = workspaceRepository.findByUserIdAndJobId(userId, application.getJob().getId()).orElse(null);
            if (workspace == null && application.getWorkspace() == null) {
                missingArtifacts.add("Workspace not generated");
            }
        }

        // 3. Validate Form Plan (if exists)
        String formStatus = "NONE";
        ApplicationFormPlanEntity formPlanEntity = formPlanRepository.findByApplicationId(applicationId).orElse(null);
        if (formPlanEntity != null) {
            try {
                ApplicationFormPlan formPlan = objectMapper.readValue(formPlanEntity.getPlanJson(), ApplicationFormPlan.class);
                formStatus = formPlan.getReadinessStatus() != null ? formPlan.getReadinessStatus().name() : "UNANALYZED";

                if (formPlan.getUnresolvedFields() != null && !formPlan.getUnresolvedFields().isEmpty()) {
                    unresolvedFields.addAll(formPlan.getUnresolvedFields());
                }

                if (formPlan.getMappings() != null) {
                    for (FieldAnswerMapping mapping : formPlan.getMappings()) {
                        if (mapping.isRequiresReview() && (mapping.getProposedValue() == null || mapping.getProposedValue().isBlank())) {
                            unresolvedFields.add(mapping.getFieldName() != null ? mapping.getFieldName() : mapping.getFieldId());
                        }
                    }
                }

                if (formPlan.getReadinessStatus() == FormReadinessStatus.REQUIRES_REVIEW || formPlan.getReadinessStatus() == FormReadinessStatus.NOT_READY) {
                    reviewReasons.add("Form plan readiness is " + formPlan.getReadinessStatus());
                }
            } catch (Exception e) {
                log.warn("Failed to parse form plan JSON for application {}", applicationId, e);
                warnings.add("Failed to parse form plan JSON");
            }
        }

        // 4. Validate Human Approval
        List<ApplicationApproval> approvals = approvalRepository.findByApplicationIdOrderByApprovedAtDesc(applicationId);
        if (approvals.isEmpty() && state != ApplicationState.APPROVED && state != ApplicationState.APPLYING) {
            reviewReasons.add("Human approval mandatory before execution");
        }

        // 5. Validate Active Executions
        List<ApplicationExecution> executions = executionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        boolean activeExecution = executions.stream().anyMatch(e -> e.getStatus() != null && ("RUNNING".equalsIgnoreCase(e.getStatus().name()) || "READY".equalsIgnoreCase(e.getStatus().name())));
        if (activeExecution) {
            reviewReasons.add("Active execution is currently running for this application");
        }

        boolean isReady = stateEligible && missingArtifacts.isEmpty() && unresolvedFields.isEmpty() && reviewReasons.isEmpty();

        return ApplicationReadinessResult.builder()
                .ready(isReady)
                .applicationId(applicationId)
                .currentState(state != null ? state.name() : "UNKNOWN")
                .formPlanStatus(formStatus)
                .missingArtifacts(missingArtifacts)
                .unresolvedFields(unresolvedFields)
                .reviewReasons(reviewReasons)
                .warnings(warnings)
                .build();
    }
}
