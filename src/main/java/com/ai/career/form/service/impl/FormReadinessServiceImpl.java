package com.ai.career.form.service.impl;

import com.ai.career.form.model.*;
import com.ai.career.form.service.FormReadinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Slf4j
@Service
public class FormReadinessServiceImpl implements FormReadinessService {

    @Override
    public FormReadinessStatus evaluateReadiness(List<NormalizedFormField> fields, List<FieldAnswerMapping> mappings) {
        if (fields == null || fields.isEmpty()) {
            return FormReadinessStatus.READY;
        }

        Map<String, FieldAnswerMapping> mappingMap = (mappings != null)
            ? mappings.stream().collect(Collectors.toMap(FieldAnswerMapping::getFieldId, m -> m, (m1, m2) -> m1))
            : Map.of();

        boolean hasUnresolvedRequired = false;
        boolean hasReviewRequired = false;

        for (NormalizedFormField field : fields) {
            FieldAnswerMapping mapping = mappingMap.get(field.getFieldId());
            boolean isRequired = field.isRequired();

            if (mapping == null || mapping.getMappingType() == MappingType.USER_REQUIRED || mapping.getMappingType() == MappingType.UNSUPPORTED) {
                if (isRequired) {
                    hasUnresolvedRequired = true;
                } else {
                    hasReviewRequired = true;
                }
            } else if (mapping.isRequiresReview() || mapping.getConfidence() < 0.90 || mapping.getMappingType() == MappingType.AI_GENERATED) {
                hasReviewRequired = true;
            }
        }

        if (hasUnresolvedRequired) {
            return FormReadinessStatus.NOT_READY;
        }
        if (hasReviewRequired) {
            return FormReadinessStatus.REQUIRES_REVIEW;
        }

        return FormReadinessStatus.READY;
    }

    @Override
    public void populateFormPlanReadiness(ApplicationFormPlan plan) {
        if (plan == null) {
            return;
        }

        List<String> unresolved = new ArrayList<>();
        List<String> reviewRequired = new ArrayList<>();

        Map<String, FieldAnswerMapping> mappingMap = (plan.getMappings() != null)
            ? plan.getMappings().stream().collect(Collectors.toMap(FieldAnswerMapping::getFieldId, m -> m, (m1, m2) -> m1))
            : Map.of();

        if (plan.getFields() != null) {
            for (NormalizedFormField field : plan.getFields()) {
                FieldAnswerMapping mapping = mappingMap.get(field.getFieldId());
                if (mapping == null || mapping.getMappingType() == MappingType.USER_REQUIRED || mapping.getMappingType() == MappingType.UNSUPPORTED) {
                    unresolved.add(field.getLabel());
                } else if (mapping.isRequiresReview() || mapping.getConfidence() < 0.90 || mapping.getMappingType() == MappingType.AI_GENERATED) {
                    reviewRequired.add(field.getLabel());
                }
            }
        }

        plan.setUnresolvedFields(unresolved);
        plan.setReviewRequiredFields(reviewRequired);
        plan.setReadinessStatus(evaluateReadiness(plan.getFields(), plan.getMappings()));
    }
}
