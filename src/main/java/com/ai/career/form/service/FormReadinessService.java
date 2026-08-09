package com.ai.career.form.service;

import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.model.FieldAnswerMapping;
import com.ai.career.form.model.FormReadinessStatus;
import com.ai.career.form.model.NormalizedFormField;

import java.util.List;

public interface FormReadinessService {
    FormReadinessStatus evaluateReadiness(List<NormalizedFormField> fields, List<FieldAnswerMapping> mappings);
    void populateFormPlanReadiness(ApplicationFormPlan plan);
}
