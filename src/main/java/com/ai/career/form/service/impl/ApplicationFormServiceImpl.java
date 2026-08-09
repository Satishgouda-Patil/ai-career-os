package com.ai.career.form.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.application.statemachine.ApplicationStateMachine;
import com.ai.career.execution.provider.FormDefinition;
import com.ai.career.execution.provider.FormField;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.form.entity.ApplicationFormPlanEntity;
import com.ai.career.form.model.*;
import com.ai.career.form.repository.ApplicationFormPlanRepository;
import com.ai.career.form.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationFormServiceImpl implements ApplicationFormService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationFormPlanRepository formPlanRepository;
    private final ApplicationExecutionProviderRegistry providerRegistry;
    private final FormNormalizationService normalizationService;
    private final FieldClassificationService classificationService;
    private final AnswerMappingService answerMappingService;
    private final FormReadinessService readinessService;
    private final ApplicationStateMachine stateMachine;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApplicationFormPlan analyzeApplicationForm(Long userId, Long applicationId) {
        log.info("Analyzing form for Application ID: {}, User ID: {}", applicationId, userId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        // 1. Resolve Provider & Raw Form Definition
        String providerName = application.getProviderName() != null ? application.getProviderName() : "MOCK";
        FormDefinition rawForm = buildDefaultFormDefinition(application);

        // 2. Form Field Normalization
        List<NormalizedFormField> normalizedFields = normalizationService.normalizeForm(rawForm);

        // 3. Field Classification
        for (NormalizedFormField field : normalizedFields) {
            FieldCategory category = classificationService.classifyField(field);
            field.setCategory(category);
        }

        // 4. Candidate Answer Mapping & Anti-Fabrication Rule
        List<FieldAnswerMapping> mappings = new ArrayList<>();
        for (NormalizedFormField field : normalizedFields) {
            mappings.add(answerMappingService.mapFieldAnswer(application, field));
        }

        // 5. Build Form Plan & Evaluate Readiness
        ApplicationFormPlan plan = ApplicationFormPlan.builder()
            .applicationId(applicationId)
            .providerName(providerName)
            .formId(rawForm.getFormId())
            .fields(normalizedFields)
            .mappings(mappings)
            .generatedAt(LocalDateTime.now())
            .build();

        readinessService.populateFormPlanReadiness(plan);

        // 6. Persist Form Plan
        saveFormPlanEntity(application, plan);

        return plan;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationFormPlan getFormPlan(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to application ID: " + applicationId);
        }

        return formPlanRepository.findByApplicationId(applicationId)
            .map(entity -> {
                try {
                    return objectMapper.readValue(entity.getPlanJson(), ApplicationFormPlan.class);
                } catch (Exception ex) {
                    log.error("Failed to parse form plan JSON for application ID: {}", applicationId, ex);
                    return analyzeApplicationForm(userId, applicationId);
                }
            })
            .orElseGet(() -> analyzeApplicationForm(userId, applicationId));
    }

    @Override
    @Transactional
    public ApplicationFormPlan approveFormPlan(Long userId, Long applicationId) {
        ApplicationFormPlan plan = getFormPlan(userId, applicationId);
        plan.setReadinessStatus(FormReadinessStatus.READY);
        plan.setUnresolvedFields(List.of());
        plan.setReviewRequiredFields(List.of());

        Application application = applicationRepository.findById(applicationId).orElseThrow();
        saveFormPlanEntity(application, plan);

        if (stateMachine.canTransition(application.getStatus(), ApplicationState.READY_FOR_REVIEW)) {
            application.setStatus(ApplicationState.READY_FOR_REVIEW);
            applicationRepository.save(application);
        }

        return plan;
    }

    private FormDefinition buildDefaultFormDefinition(Application application) {
        List<FormField> fields = List.of(
            FormField.builder().name("full_name").label("Full Name").type("TEXT").required(true).build(),
            FormField.builder().name("email").label("Email Address").type("EMAIL").required(true).build(),
            FormField.builder().name("phone").label("Phone Number").type("PHONE").required(false).build(),
            FormField.builder().name("linkedin_url").label("LinkedIn URL").type("URL").required(false).build(),
            FormField.builder().name("resume").label("Attach Resume").type("FILE").required(true).build(),
            FormField.builder().name("cover_letter").label("Cover Letter").type("TEXTAREA").required(false).build(),
            FormField.builder().name("why_hire").label("Why do you want to work at " + (application.getJob() != null ? application.getJob().getCompany() : "our company") + "?").type("TEXTAREA").required(true).build(),
            FormField.builder().name("work_authorization").label("Are you legally authorized to work in the United States?").type("SELECT").options(List.of("Yes", "No")).required(true).build()
        );

        return FormDefinition.builder()
            .formId("form_" + application.getId())
            .actionUrl(application.getApplicationUrl())
            .fields(fields)
            .hasCaptcha(false)
            .hasMfa(false)
            .build();
    }

    private void saveFormPlanEntity(Application application, ApplicationFormPlan plan) {
        try {
            String json = objectMapper.writeValueAsString(plan);
            ApplicationFormPlanEntity entity = formPlanRepository.findByApplicationId(application.getId())
                .orElse(ApplicationFormPlanEntity.builder().application(application).build());

            entity.setReadinessStatus(plan.getReadinessStatus());
            entity.setPlanJson(json);

            formPlanRepository.save(entity);
        } catch (Exception ex) {
            log.error("Error serializing form plan for application ID: {}", application.getId(), ex);
        }
    }
}
