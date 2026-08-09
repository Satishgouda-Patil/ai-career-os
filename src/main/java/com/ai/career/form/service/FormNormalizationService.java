package com.ai.career.form.service;

import com.ai.career.execution.provider.FormDefinition;
import com.ai.career.execution.provider.FormField;
import com.ai.career.form.model.NormalizedFormField;

import java.util.List;

public interface FormNormalizationService {
    List<NormalizedFormField> normalizeForm(FormDefinition formDefinition);
    NormalizedFormField normalizeField(FormField formField, int index);
}
