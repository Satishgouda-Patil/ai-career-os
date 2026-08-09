package com.ai.career.validation.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.form.model.*;
import com.ai.career.validation.model.*;
import com.ai.career.validation.service.FormValidationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormValidationEngineImpl implements FormValidationEngine {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\s-]{7,20}$");

    private final ApplicationExecutionProviderRegistry providerRegistry;

    @Override
    public ApplicationValidationResult validateApplication(Application application, ApplicationFormPlan formPlan) {
        log.info("Performing deterministic validation for application ID: {}", application != null ? application.getId() : null);

        List<ValidationErrorReason> reasons = new ArrayList<>();

        if (application == null) {
            reasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.APPLICATION_STATE_INVALID)
                .field("application")
                .message("Application is null")
                .build());
            return buildResult(application != null ? application.getId() : null, false, ApplicationValidationStatus.INVALID, ExecutionReadiness.BLOCKED, reasons);
        }

        // 1. Application State Safety Check
        ApplicationState state = application.getStatus();
        if (state == ApplicationState.DISCOVERED || state == ApplicationState.QUALIFIED || state == ApplicationState.PREPARING
            || state == ApplicationState.FAILED || state == ApplicationState.CLOSED || state == ApplicationState.WITHDRAWN || state == ApplicationState.REJECTED) {
            reasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.APPLICATION_STATE_INVALID)
                .field("application.status")
                .message("Application state [" + state + "] does not permit execution readiness.")
                .build());
        }

        if (formPlan == null || formPlan.getFields() == null || formPlan.getFields().isEmpty()) {
            reasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.REQUIRED_FIELD_MISSING)
                .field("formPlan")
                .message("Form plan is missing or contains no fields")
                .build());
            return buildResult(application.getId(), false, ApplicationValidationStatus.INVALID, ExecutionReadiness.BLOCKED, reasons);
        }

        Map<String, FieldAnswerMapping> mappingMap = (formPlan.getMappings() != null)
            ? formPlan.getMappings().stream().collect(Collectors.toMap(FieldAnswerMapping::getFieldId, m -> m, (m1, m2) -> m1))
            : Map.of();

        boolean hasBlockingError = false;
        boolean hasReviewReason = false;

        // 2. Validate Form Fields & Answer Mappings
        for (NormalizedFormField field : formPlan.getFields()) {
            FieldAnswerMapping mapping = mappingMap.get(field.getFieldId());
            boolean isRequired = field.isRequired();

            if (mapping == null) {
                if (isRequired) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.REQUIRED_FIELD_MISSING)
                        .field(field.getLabel())
                        .message("Required form field has no candidate answer mapping")
                        .build());
                    hasBlockingError = true;
                }
                continue;
            }

            if (mapping.getMappingType() == MappingType.USER_REQUIRED) {
                if (isRequired) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.REQUIRED_FIELD_MISSING)
                        .field(field.getLabel())
                        .message("Required field candidate data unavailable. User input required.")
                        .build());
                    hasBlockingError = true;
                } else {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.SENSITIVE_FIELD_REQUIRES_REVIEW)
                        .field(field.getLabel())
                        .message("Optional field candidate data unavailable. Review suggested.")
                        .build());
                    hasReviewReason = true;
                }
                continue;
            }

            if (mapping.getMappingType() == MappingType.UNSUPPORTED) {
                reasons.add(ValidationErrorReason.builder()
                    .code(ValidationErrorCode.UNSUPPORTED_FIELD)
                    .field(field.getLabel())
                    .message("Unsupported field type cannot be answered automatically")
                    .build());
                if (isRequired) hasBlockingError = true; else hasReviewReason = true;
                continue;
            }

            // 3. Field Format & Type Validation
            String val = mapping.getProposedValue();
            if (val != null && !val.isBlank()) {
                validateFieldFormat(field, val, reasons);
            } else if (isRequired) {
                reasons.add(ValidationErrorReason.builder()
                    .code(ValidationErrorCode.REQUIRED_FIELD_MISSING)
                    .field(field.getLabel())
                    .message("Proposed answer value is blank for required field")
                    .build());
                hasBlockingError = true;
            }

            // 4. Sensitive Field Protection
            if (field.getCategory() == FieldCategory.WORK_AUTHORIZATION || field.getCategory() == FieldCategory.VISA || field.getCategory() == FieldCategory.SALARY) {
                reasons.add(ValidationErrorReason.builder()
                    .code(ValidationErrorCode.SENSITIVE_FIELD_REQUIRES_REVIEW)
                    .field(field.getLabel())
                    .message("Sensitive field requires explicit candidate review before submission")
                    .build());
                hasReviewReason = true;
            }

            // 5. AI-Generated Answer Protection
            if (mapping.getMappingType() == MappingType.AI_GENERATED) {
                reasons.add(ValidationErrorReason.builder()
                    .code(ValidationErrorCode.AI_ANSWER_REQUIRES_REVIEW)
                    .field(field.getLabel())
                    .message("AI-generated response requires candidate approval")
                    .build());
                hasReviewReason = true;
            }
        }

        // 6. Resume & Cover Letter Artifact Validation
        boolean resumeRequired = formPlan.getFields().stream().anyMatch(f -> f.getCategory() == FieldCategory.RESUME && f.isRequired());
        if (resumeRequired && (application.getResumeVersion() == null || application.getResumeVersion().getPdfUrl() == null)) {
            reasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.RESUME_MISSING)
                .field("resume")
                .message("Required resume file artifact is missing or inaccessible")
                .build());
            hasBlockingError = true;
        }

        // 7. Provider Capability Check
        try {
            providerRegistry.resolve(application);
        } catch (Exception ex) {
            reasons.add(ValidationErrorReason.builder()
                .code(ValidationErrorCode.PROVIDER_CAPABILITY_MISSING)
                .field("provider")
                .message("Provider resolution failed for application ID: " + application.getId())
                .build());
            hasBlockingError = true;
        }

        // Determine final validation status and readiness
        ApplicationValidationStatus valStatus;
        ExecutionReadiness readiness;

        if (hasBlockingError || reasons.stream().anyMatch(r -> r.getCode() == ValidationErrorCode.APPLICATION_STATE_INVALID)) {
            valStatus = ApplicationValidationStatus.INVALID;
            readiness = ExecutionReadiness.BLOCKED;
        } else if (hasReviewReason || !reasons.isEmpty()) {
            valStatus = ApplicationValidationStatus.REQUIRES_REVIEW;
            readiness = ExecutionReadiness.REQUIRES_REVIEW;
        } else {
            valStatus = ApplicationValidationStatus.VALID;
            readiness = ExecutionReadiness.READY_FOR_EXECUTION;
        }

        return buildResult(application.getId(), valStatus == ApplicationValidationStatus.VALID, valStatus, readiness, reasons);
    }

    private void validateFieldFormat(NormalizedFormField field, String value, List<ValidationErrorReason> reasons) {
        switch (field.getType()) {
            case EMAIL:
                if (!EMAIL_PATTERN.matcher(value).matches()) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.INVALID_EMAIL)
                        .field(field.getLabel())
                        .message("Proposed email value [" + value + "] fails email format validation")
                        .build());
                }
                break;

            case PHONE:
                if (!PHONE_PATTERN.matcher(value).matches()) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.INVALID_PHONE)
                        .field(field.getLabel())
                        .message("Proposed phone value fails phone structure validation")
                        .build());
                }
                break;

            case URL:
                try {
                    URI uri = new URI(value);
                    if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                        throw new IllegalArgumentException("Invalid URI scheme");
                    }
                } catch (Exception ex) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.INVALID_URL)
                        .field(field.getLabel())
                        .message("Proposed URL [" + value + "] is not a valid HTTP/HTTPS URL")
                        .build());
                }
                break;

            case NUMBER:
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    reasons.add(ValidationErrorReason.builder()
                        .code(ValidationErrorCode.INVALID_NUMBER)
                        .field(field.getLabel())
                        .message("Proposed value [" + value + "] is not a valid number")
                        .build());
                }
                break;

            case SELECT:
            case RADIO:
                if (field.getOptions() != null && !field.getOptions().isEmpty()) {
                    boolean matched = field.getOptions().stream().anyMatch(opt -> opt.equalsIgnoreCase(value));
                    if (!matched) {
                        reasons.add(ValidationErrorReason.builder()
                            .code(ValidationErrorCode.INVALID_OPTION)
                            .field(field.getLabel())
                            .message("Proposed answer [" + value + "] does not match any available provider options: " + field.getOptions())
                            .build());
                    }
                }
                break;

            default:
                break;
        }
    }

    private ApplicationValidationResult buildResult(Long applicationId, boolean valid, ApplicationValidationStatus status, ExecutionReadiness readiness, List<ValidationErrorReason> reasons) {
        return ApplicationValidationResult.builder()
            .applicationId(applicationId)
            .valid(valid)
            .status(status)
            .readiness(readiness)
            .reasons(reasons)
            .validatedAt(LocalDateTime.now())
            .build();
    }
}
