package com.ai.career.tracking.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateAnswerRequest {
    private Long mockSessionId;
    private String question;
    private String candidateAnswer;
    private String questionCategory;
}
