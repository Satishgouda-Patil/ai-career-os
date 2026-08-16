package com.ai.career.tracking.interview.service;

import com.ai.career.tracking.interview.dto.InterviewDto;
import com.ai.career.tracking.interview.dto.InterviewPrepDto;
import com.ai.career.tracking.interview.dto.ScheduleInterviewRequest;

import java.util.List;

public interface InterviewPrepService {
    InterviewDto createInterview(Long userId, Long applicationId, ScheduleInterviewRequest request);
    InterviewPrepDto generatePrepWorkspace(Long userId, Long interviewId);
    InterviewPrepDto getInterviewPrep(Long userId, Long interviewId);
    InterviewDto getInterviewDetails(Long userId, Long interviewId);
    List<InterviewDto> getApplicationInterviews(Long userId, Long applicationId);
    List<InterviewDto> getUserInterviews(Long userId);
}
