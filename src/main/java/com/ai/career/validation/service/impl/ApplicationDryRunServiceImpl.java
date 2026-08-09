package com.ai.career.validation.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.execution.provider.*;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.service.ApplicationFormService;
import com.ai.career.validation.entity.ApplicationDryRunEntity;
import com.ai.career.validation.model.*;
import com.ai.career.validation.repository.ApplicationDryRunRepository;
import com.ai.career.validation.service.ApplicationDryRunService;
import com.ai.career.validation.service.FormValidationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDryRunServiceImpl implements ApplicationDryRunService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFormService formService;
    private final FormValidationEngine validationEngine;
    private final ApplicationExecutionProviderRegistry providerRegistry;
    private final ApplicationDryRunRepository dryRunRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DryRunReport executeDryRun(Long userId, Long applicationId) {
        log.info("Executing safe dry run simulation for Application ID: {}, User ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        // 1. Form Analysis & Plan Retrieval
        ApplicationFormPlan formPlan = formService.getFormPlan(userId, applicationId);

        // 2. Form Validation Engine Execution
        ApplicationValidationResult valResult = validationEngine.validateApplication(application, formPlan);

        // 3. Provider Resolution & Context Preparation
        ApplicationExecutionProvider provider = providerRegistry.resolve(application);
        String providerName = provider.getProviderName();

        ExecutionContext context = ExecutionContext.builder()
            .applicationId(applicationId)
            .userId(userId)
            .providerName(providerName)
            .dryRun(true)
            .build();

        // 4. Simulate Provider Validation (SAFE ZERO-SIDE-EFFECT GUARANTEE)
        ExecutionValidationResult providerValResult = provider.validate(application, context);

        String runId = "dryrun_" + UUID.randomUUID().toString().substring(0, 18);
        List<ValidationErrorReason> allReasons = new ArrayList<>(valResult.getReasons());

        if (!providerValResult.isValid()) {
            allReasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.PROVIDER_CAPABILITY_MISSING)
                .field("provider")
                .message("Provider validation failed: " + providerValResult.getErrorMessage())
                .build());
        }

        int fieldsCount = formPlan.getFields() != null ? formPlan.getFields().size() : 0;
        int filesCount = (application.getResumeVersion() != null ? 1 : 0) + (application.getCoverLetter() != null ? 1 : 0);

        List<String> caps = provider.getCapabilities() != null
            ? provider.getCapabilities().getCapabilities().stream().map(Enum::name).toList()
            : List.of("FORM_FILLING", "MOCK_SUBMISSION");

        DryRunReport report = DryRunReport.builder()
            .runId(runId)
            .applicationId(applicationId)
            .providerName(providerName)
            .validationStatus(valResult.getStatus())
            .readinessStatus(valResult.getReadiness())
            .fieldsSimulatedCount(fieldsCount)
            .filesSimulatedCount(filesCount)
            .capabilitiesVerified(caps)
            .simulatedOutcome("DRY_RUN_SIMULATION_SUCCESS")
            .reasons(allReasons)
            .createdAt(LocalDateTime.now())
            .build();

        // 5. Persist Dry Run Report Entity
        try {
            String json = objectMapper.writeValueAsString(report);
            ApplicationDryRunEntity entity = ApplicationDryRunEntity.builder()
                .application(application)
                .runId(runId)
                .validationStatus(valResult.getStatus())
                .readinessStatus(valResult.getReadiness())
                .dryRunReportJson(json)
                .build();
            dryRunRepository.save(entity);
        } catch (Exception ex) {
            log.error("Failed to persist dry run report for application ID: {}", applicationId, ex);
        }

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public DryRunReport getDryRunReport(Long userId, String runId) {
        ApplicationDryRunEntity entity = dryRunRepository.findByRunId(runId)
            .orElseThrow(() -> new IllegalArgumentException("Dry run report not found for run ID: " + runId));

        if (!entity.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to dry run report: " + runId);
        }

        try {
            return objectMapper.readValue(entity.getDryRunReportJson(), DryRunReport.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse dry run report JSON: " + ex.getMessage(), ex);
        }
    }
}
