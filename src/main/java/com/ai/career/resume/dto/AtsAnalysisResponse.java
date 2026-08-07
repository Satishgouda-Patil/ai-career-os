package com.ai.career.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtsAnalysisResponse {
    private Long id;
    private Long resumeVersionId;
    private Integer overallScore;
    private Integer keywordScore;
    private Integer formatScore;
    private Integer readabilityScore;
    private List<String> missingKeywords;
    private List<String> recommendations;
}
