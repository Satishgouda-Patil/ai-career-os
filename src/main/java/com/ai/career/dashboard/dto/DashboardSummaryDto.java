package com.ai.career.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalJobsDiscovered;
    private long highMatchJobsCount;
    private long totalApplications;
    private long reviewRequiredCount;
    private long approvedApplicationsCount;
    private long followUpsDueCount;
    private long upcomingInterviewsCount;
    private Map<String, Long> applicationsByStatus;
    private List<Map<String, Object>> recentActivities;
}
