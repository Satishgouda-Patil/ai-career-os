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
public class LiveExecutionPrepareResponse {
    private Long applicationId;
    private String mode;
    private String provider;
    private String status;
    private String targetUrl;
    private int fieldsDetected;
    private int fieldsMapped;
    private int fieldsRequireReview;
    private boolean submissionAttempted;
    private String previewId;
    private String formFingerprint;
    private boolean readyForSubmission;

    @Builder.Default
    private List<String> unresolvedFields = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
