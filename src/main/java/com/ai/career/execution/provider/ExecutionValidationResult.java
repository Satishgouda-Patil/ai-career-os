package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionValidationResult {
    private boolean valid;
    private List<String> missingFields;
    private List<String> validationErrors;
    private String errorMessage;
}
