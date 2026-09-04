package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionMetricsResponse {
    private long totalExecutions;
    private long sandboxExecutions;
    private long readOnlyExecutions;
    private long liveExecutions;
    private long blockedExecutions;
    private long failedExecutions;
    private long successfulExecutions;
    private long unknownOutcomeExecutions;
    private long retryableExecutions;
    private long realSubmissions;
    private long emailsSent;
    private long filesUploaded;
}
