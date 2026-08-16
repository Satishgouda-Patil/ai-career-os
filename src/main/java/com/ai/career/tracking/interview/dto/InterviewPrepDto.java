package com.ai.career.tracking.interview.dto;

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
public class InterviewPrepDto {
    private Long id;
    private Long interviewId;
    private Map<String, Object> companyOverview;
    private Map<String, Object> roleFocus;
    private Map<String, Object> candidateTalkingPoints;
    private List<Map<String, String>> sampleQuestions;
    private List<String> questionsToAsk;
    private LocalDateTime generatedAt;
}
