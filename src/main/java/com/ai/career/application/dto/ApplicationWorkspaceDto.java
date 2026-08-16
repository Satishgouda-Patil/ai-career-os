package com.ai.career.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationWorkspaceDto {

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String jobDescription;
    private String applicationStatus;
    private Integer fitScore;

    // Artifacts & Form Readiness
    private String resumeUrl;
    private String coverLetterText;
    private boolean formPlanReady;
    private int totalFieldsMapped;
    private List<Map<String, Object>> formFields;

    // Safety & Governance
    private boolean autoApplyEnabled;
    private boolean candidateApproved;
    private String currentLockOwner;

    // Activity & Audit Timeline
    private List<Map<String, Object>> activityTimeline;
    private List<Map<String, Object>> workflowExecutionRuns;
}
