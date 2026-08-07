package com.ai.career.jobanalysis.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.jobanalysis.dto.JobAnalysisResponse;
import com.ai.career.jobanalysis.dto.JobRecommendationDto;
import com.ai.career.jobanalysis.dto.MissingSkillDto;
import com.ai.career.jobanalysis.service.JobAnalysisService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Intelligence", description = "Deep Job Analysis, Skill Gap Analysis & Recommendation Engine APIs")
public class JobAnalysisController {

    private final JobAnalysisService jobAnalysisService;

    @PostMapping("/{jobId}/analyze")
    @Operation(summary = "Analyze job posting", description = "Triggers AI deep analysis to extract responsibilities, skills, salary, work model, and match score")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeJob(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        JobAnalysisResponse analysis = jobAnalysisService.analyzeJob(userId, jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(Map.of("analysisId", analysis.getId(), "status", "COMPLETED")));
    }

    @GetMapping("/{jobId}/analysis")
    @Operation(summary = "Get job analysis", description = "Returns deep analysis details for specified job")
    public ResponseEntity<ApiResponse<JobAnalysisResponse>> getJobAnalysis(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        JobAnalysisResponse analysis = jobAnalysisService.getJobAnalysis(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    @GetMapping("/{jobId}/missing-skills")
    @Operation(summary = "Get missing skills", description = "Returns prioritized missing skills gap analysis")
    public ResponseEntity<ApiResponse<List<MissingSkillDto>>> getMissingSkills(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        List<MissingSkillDto> missingSkills = jobAnalysisService.getMissingSkills(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(missingSkills));
    }

    @GetMapping("/{jobId}/recommendation")
    @Operation(summary = "Get job application recommendation", description = "Returns APPLY / WAIT / SKIP decision with confidence and rationale")
    public ResponseEntity<ApiResponse<JobRecommendationDto>> getJobRecommendation(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @PathVariable Long jobId
    ) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        JobRecommendationDto recommendation = jobAnalysisService.getJobRecommendation(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(recommendation));
    }
}
