package com.ai.career.validation.service;

import com.ai.career.validation.model.DryRunReport;

public interface ApplicationDryRunService {
    DryRunReport executeDryRun(Long userId, Long applicationId);
    DryRunReport getDryRunReport(Long userId, String runId);
}
