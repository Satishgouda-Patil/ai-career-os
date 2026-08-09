package com.ai.career.form.service;

import com.ai.career.form.model.ApplicationFormPlan;

public interface ApplicationFormService {
    ApplicationFormPlan analyzeApplicationForm(Long userId, Long applicationId);
    ApplicationFormPlan getFormPlan(Long userId, Long applicationId);
    ApplicationFormPlan approveFormPlan(Long userId, Long applicationId);
}
