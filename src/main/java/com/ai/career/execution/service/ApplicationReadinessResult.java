package com.ai.career.execution.service;

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
public class ApplicationReadinessResult {

    private boolean ready;
    private Long applicationId;
    private String currentState;
    private String formPlanStatus;
    
    @Builder.Default
    private List<String> missingArtifacts = new ArrayList<>();
    
    @Builder.Default
    private List<String> unresolvedFields = new ArrayList<>();
    
    @Builder.Default
    private List<String> reviewReasons = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
