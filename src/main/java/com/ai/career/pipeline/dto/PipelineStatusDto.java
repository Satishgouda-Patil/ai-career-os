package com.ai.career.pipeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatusDto {

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private int currentStep;
    private int totalSteps;
    private String currentStepName;
    private int progressPercentage;
    private String status; // RUNNING, COMPLETED, FAILED
    private List<String> logs;
    private List<String> artifactsGenerated;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
