package com.ai.career.form.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.form.model.FieldAnswerMapping;
import com.ai.career.form.model.NormalizedFormField;

public interface AnswerMappingService {
    FieldAnswerMapping mapFieldAnswer(Application application, NormalizedFormField field);
}
