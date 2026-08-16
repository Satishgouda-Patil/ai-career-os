package com.ai.career.execution.provider;

import com.ai.career.application.domain.entity.Application;

public interface ApplicationExecutionProvider {

    boolean supports(Application application);

    String getProviderName();

    ProviderCapabilities getCapabilities();

    ExecutionValidationResult validate(Application application, ExecutionContext context);

    ExecutionResult execute(Application application, ExecutionContext context);
}
