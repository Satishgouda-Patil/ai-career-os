package com.ai.career.browser.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionPreview {
    private Long applicationId;
    private String formId;
    private boolean submitControlDetected;

    /**
     * Always false for M6-B safe dry-runs.
     */
    @Builder.Default
    private boolean readyForSubmission = false;

    @Builder.Default
    private Map<String, String> filledFields = Map.of();

    @Builder.Default
    private List<String> uploadedFiles = new ArrayList<>();

    @Builder.Default
    private List<String> unresolvedFields = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    private String status;
}
