package com.ai.career.dashboard.controller;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.common.dto.ApiResponse;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.JobMatchRepository;
import com.ai.career.security.UserPrincipal;
import com.ai.career.dashboard.dto.DashboardSummaryDto;
import com.ai.career.tracking.domain.repository.ApplicationActivityRepository;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JobRepository jobRepository;
    private final JobMatchRepository jobMatchRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationActivityRepository activityRepository;
    private final ApplicationFollowUpRepository followUpRepository;
    private final InterviewRepository interviewRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();

        long totalJobsDiscovered = jobRepository.count();
        long highMatchJobs = jobMatchRepository.findMatchedJobsForProfile(userId, 80).size();

        List<Application> userApps = applicationRepository.findByUserId(userId);
        long totalApplications = userApps.size();

        Map<String, Long> statusCounts = userApps.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));

        long reviewRequired = userApps.stream()
                .filter(a -> a.getStatus() == ApplicationState.READY_FOR_REVIEW || a.getStatus() == ApplicationState.SUBMISSION_REQUIRES_REVIEW)
                .count();

        long approvedApps = userApps.stream()
                .filter(a -> a.getStatus() == ApplicationState.APPROVED)
                .count();

        long followUpsDue = userApps.stream()
                .flatMap(a -> followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(a.getId()).stream())
                .filter(f -> "SCHEDULED".equalsIgnoreCase(f.getStatus()) || "READY".equalsIgnoreCase(f.getStatus()))
                .count();

        long upcomingInterviews = interviewRepository.findByApplicationUserIdOrderByScheduledAtDesc(userId).stream()
                .filter(i -> "SCHEDULED".equalsIgnoreCase(i.getStatus()))
                .count();

        List<Map<String, Object>> recentActivities = userApps.stream()
                .flatMap(a -> activityRepository.findByApplicationIdOrderByCreatedAtDesc(a.getId()).stream())
                .sorted(Comparator.comparing(a -> a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(10)
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("applicationId", a.getApplication().getId());
                    map.put("company", a.getApplication().getJob() != null ? a.getApplication().getJob().getCompany() : "Company");
                    map.put("jobTitle", a.getApplication().getJob() != null ? a.getApplication().getJob().getTitle() : "Position");
                    map.put("activityType", a.getActivityType());
                    map.put("description", a.getDescription());
                    map.put("createdAt", a.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        DashboardSummaryDto summary = DashboardSummaryDto.builder()
                .totalJobsDiscovered(totalJobsDiscovered)
                .highMatchJobsCount(highMatchJobs)
                .totalApplications(totalApplications)
                .reviewRequiredCount(reviewRequired)
                .approvedApplicationsCount(approvedApps)
                .followUpsDueCount(followUpsDue)
                .upcomingInterviewsCount(upcomingInterviews)
                .applicationsByStatus(statusCounts)
                .recentActivities(recentActivities)
                .build();

        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved successfully"));
    }
}
