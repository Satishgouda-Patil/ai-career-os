package com.ai.career.job.controller;

import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.JobMatch;
import com.ai.career.domain.repository.JobMatchRepository;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.job.service.JobIngestionService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Matching", description = "Endpoints for retrieving matched jobs, job details, and manual fetch triggers")
public class JobController {

    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final JobIngestionService jobIngestionService;

    @GetMapping
    @Operation(summary = "Get matched jobs for current user filtered by minimum match score")
    public ResponseEntity<List<MatchedJobResponse>> getMatchedJobs(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestParam(value = "minScore", defaultValue = "0") Integer minScore
    ) {
        List<JobMatch> matches = jobMatchRepository.findMatchedJobsForProfile(userPrincipal.getId(), minScore);

        List<MatchedJobResponse> response = matches.stream()
            .map(m -> MatchedJobResponse.builder()
                .matchId(m.getId())
                .jobId(m.getJob().getId())
                .source(m.getJob().getSource())
                .title(m.getJob().getTitle())
                .company(m.getJob().getCompany())
                .location(m.getJob().getLocation())
                .url(m.getJob().getUrl())
                .score(m.getScore())
                .postedAt(m.getJob().getPostedAt())
                .matchedAt(m.getMatchedAt())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed job description by job ID")
    public ResponseEntity<Job> getJobDetails(@PathVariable("id") Long id) {
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Job posting not found for ID: " + id));
        return ResponseEntity.ok(job);
    }

    @PostMapping("/fetch")
    @Operation(summary = "Manually trigger external job fetcher connector")
    public ResponseEntity<Map<String, Object>> triggerFetch(
        @RequestParam(value = "keywords", defaultValue = "Java") String keywords,
        @RequestParam(value = "location", defaultValue = "Remote") String location
    ) {
        int ingestedCount = jobIngestionService.triggerJobFetch(keywords, location);
        return ResponseEntity.ok(Map.of(
            "message", "Job ingestion triggered successfully",
            "ingestedCount", ingestedCount
        ));
    }

    @Data
    @Builder
    public static class MatchedJobResponse {
        private Long matchId;
        private Long jobId;
        private String source;
        private String title;
        private String company;
        private String location;
        private String url;
        private Integer score;
        private LocalDateTime postedAt;
        private LocalDateTime matchedAt;
    }
}
