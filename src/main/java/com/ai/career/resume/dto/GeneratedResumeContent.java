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
public class GeneratedResumeContent {
    private String fullName;
    private String headline;
    private String summary;
    private List<String> skills;
    private List<ExperienceItem> experience;
    private Integer atsScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceItem {
        private String title;
        private String company;
        private String period;
        private List<String> highlights;
    }
}
