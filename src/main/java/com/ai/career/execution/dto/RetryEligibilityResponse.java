package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryEligibilityResponse {
    private Long runId;
    private boolean eligible;
    private String reason;
    private boolean submissionAttempted;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
