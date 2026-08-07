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
public class JobRecommendationDto {
    private String recommendation;
    private String decision;
    private Integer confidence;
    private List<String> reason;
    private String rationale;
}
