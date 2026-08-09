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
public class DryRunReport {
    private String runId;
    private Long applicationId;
    private String providerName;
    private ApplicationValidationStatus validationStatus;
    private ExecutionReadiness readinessStatus;
    private int fieldsSimulatedCount;
    private int filesSimulatedCount;
    private List<String> capabilitiesVerified;
    private String simulatedOutcome;
    private List<ValidationErrorReason> reasons;
    private LocalDateTime createdAt;
}
