package com.ai.career.tracking.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewDto {
    private Long id;
    private Long interviewId;
    private String question;
    private String questionCategory;
    private String candidateAnswer;
    private Integer score;
    private String feedback;
    private String improvedAnswer;
    private LocalDateTime evaluatedAt;
    private LocalDateTime createdAt;
}
