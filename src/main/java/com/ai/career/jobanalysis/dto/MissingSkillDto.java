package com.ai.career.jobanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingSkillDto {
    private String skill;
    private String priority;
    private String learningSuggestion;
}
