package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private ExecutionOutcomeStatus status;
    private String providerName;
    private String externalApplicationId;
    private String externalUrl;
    private String errorCode;
    private String errorMessage;
    private boolean retryable;
    private Map<String, Object> metadata;
    private LocalDateTime executedAt;

    public boolean isSuccessful() {
        return status == ExecutionOutcomeStatus.SUCCESS;
    }
}
