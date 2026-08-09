package com.ai.career.validation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationValidationResult {
    private Long applicationId;
    private boolean valid;
    private ApplicationValidationStatus status;
    private ExecutionReadiness readiness;
    private List<ValidationErrorReason> reasons;
    private LocalDateTime validatedAt;
}
