package com.ai.career.tracking.dto;

import com.ai.career.tracking.service.NextActionDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTrackingSummary {
    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String currentStatus;
    private LocalDateTime appliedAt;
    private Long ageInDays;
    private LocalDateTime lastActivityAt;
    private NextActionDecision nextActionDecision;
    private List<ActivityDto> timeline;
}
