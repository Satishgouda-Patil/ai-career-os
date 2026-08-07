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
public class JobAnalysisResult {
    private String summary;
    private List<String> responsibilities;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private String salaryRange;
    private String workModel;
    private String seniorityLevel;
    private Integer matchScore;
    private String recommendationStatus;
    private String decision;
    private Integer confidence;
    private String rationale;
    private List<AiMissingSkill> missingSkills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiMissingSkill {
        private String skillName;
        private String priority;
        private String learning_suggestion;
    }
}
