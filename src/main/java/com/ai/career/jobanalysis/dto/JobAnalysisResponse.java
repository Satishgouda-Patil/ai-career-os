package com.ai.career.jobanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysisResponse {
    private Long id;
    private Long jobId;
    private String summary;
    private List<String> responsibilities;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private Object salary;
    private String salaryRange;
    private String workModel;
    private String seniorityLevel;
    private Integer matchScore;
    private String recommendation;
    private List<MissingSkillDto> missingSkills;
    private JobRecommendationDto recommendationDetails;
}
