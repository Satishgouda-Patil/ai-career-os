package com.ai.career.tracking.interview.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.security.UserPrincipal;
import com.ai.career.tracking.interview.dto.*;
import com.ai.career.tracking.interview.service.InterviewPrepService;
import com.ai.career.tracking.interview.service.MockInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewPrepService prepService;
    private final MockInterviewService mockService;

    @PostMapping("/applications/{id}/interviews")
    public ResponseEntity<ApiResponse<InterviewDto>> createInterview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId,
            @RequestBody(required = false) ScheduleInterviewRequest request) {
        InterviewDto interview = prepService.createInterview(userPrincipal.getId(), applicationId, request);
        return ResponseEntity.ok(ApiResponse.success(interview, "Interview scheduled and preparation workspace initialized"));
    }

    @GetMapping("/interviews")
    public ResponseEntity<ApiResponse<List<InterviewDto>>> getUserInterviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<InterviewDto> interviews = prepService.getUserInterviews(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(interviews, "User interviews retrieved successfully"));
    }

    @GetMapping("/applications/{id}/interviews")
    public ResponseEntity<ApiResponse<List<InterviewDto>>> getApplicationInterviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long applicationId) {
        List<InterviewDto> interviews = prepService.getApplicationInterviews(userPrincipal.getId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(interviews, "Application interviews retrieved successfully"));
    }

    @GetMapping("/interviews/{id}")
    public ResponseEntity<ApiResponse<InterviewDto>> getInterviewDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId) {
        InterviewDto interview = prepService.getInterviewDetails(userPrincipal.getId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success(interview, "Interview details retrieved successfully"));
    }

    @GetMapping("/interviews/{id}/prep")
    public ResponseEntity<ApiResponse<InterviewPrepDto>> getInterviewPrep(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId) {
        InterviewPrepDto prep = prepService.getInterviewPrep(userPrincipal.getId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success(prep, "Interview prep workspace retrieved successfully"));
    }

    @PostMapping("/interviews/{id}/prep/generate")
    public ResponseEntity<ApiResponse<InterviewPrepDto>> generatePrepWorkspace(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId) {
        InterviewPrepDto prep = prepService.generatePrepWorkspace(userPrincipal.getId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success(prep, "Interview prep workspace generated successfully"));
    }

    @PostMapping("/interviews/{id}/mock/question")
    public ResponseEntity<ApiResponse<MockInterviewDto>> generatePracticeQuestion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId,
            @RequestBody(required = false) Map<String, String> request) {
        String category = request != null ? request.get("category") : "TECHNICAL";
        MockInterviewDto session = mockService.generatePracticeQuestion(userPrincipal.getId(), interviewId, category);
        return ResponseEntity.ok(ApiResponse.success(session, "Mock interview practice question generated successfully"));
    }

    @PostMapping("/interviews/{id}/mock/evaluate")
    public ResponseEntity<ApiResponse<MockInterviewDto>> evaluateCandidateAnswer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId,
            @RequestBody EvaluateAnswerRequest request) {
        MockInterviewDto evaluated = mockService.evaluateCandidateAnswer(userPrincipal.getId(), interviewId, request);
        return ResponseEntity.ok(ApiResponse.success(evaluated, "Mock interview answer evaluated successfully"));
    }

    @GetMapping("/interviews/{id}/mock")
    public ResponseEntity<ApiResponse<List<MockInterviewDto>>> getInterviewMockSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long interviewId) {
        List<MockInterviewDto> sessions = mockService.getInterviewMockSessions(userPrincipal.getId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Mock interview sessions retrieved successfully"));
    }
}
