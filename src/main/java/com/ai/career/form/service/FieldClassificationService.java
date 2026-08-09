package com.ai.career.form.service;

import com.ai.career.form.model.FieldCategory;
import com.ai.career.form.model.NormalizedFormField;

public interface FieldClassificationService {
    FieldCategory classifyField(NormalizedFormField field);
    double getClassificationConfidence(NormalizedFormField field, FieldCategory category);
}
