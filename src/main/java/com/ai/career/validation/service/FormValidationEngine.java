package com.ai.career.validation.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.validation.model.ApplicationValidationResult;

public interface FormValidationEngine {
    ApplicationValidationResult validateApplication(Application application, ApplicationFormPlan formPlan);
}
