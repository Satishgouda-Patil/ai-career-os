package com.ai.career.tracking.interview.service;

import com.ai.career.tracking.interview.dto.EvaluateAnswerRequest;
import com.ai.career.tracking.interview.dto.MockInterviewDto;

import java.util.List;

public interface MockInterviewService {
    MockInterviewDto generatePracticeQuestion(Long userId, Long interviewId, String category);
    MockInterviewDto evaluateCandidateAnswer(Long userId, Long interviewId, EvaluateAnswerRequest request);
    List<MockInterviewDto> getInterviewMockSessions(Long userId, Long interviewId);
}
