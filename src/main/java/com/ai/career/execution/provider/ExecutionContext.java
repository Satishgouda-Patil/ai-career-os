package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionContext {
    private Long executionId;
    private Long applicationId;
    private Long userId;
    private Long jobId;
    private String providerName;
    private String jobUrl;
    private String resumeUrl;
    private String coverLetterContent;
    private Map<String, String> applicationAnswers;
    private String automationLevel;
    private boolean dryRun;
    private String correlationId;
}
