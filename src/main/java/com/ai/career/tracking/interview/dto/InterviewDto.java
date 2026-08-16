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
public class InterviewDto {
    private Long id;
    private Long applicationId;
    private String companyName;
    private String jobTitle;
    private String interviewType;
    private LocalDateTime scheduledAt;
    private String timezone;
    private String meetingUrl;
    private String interviewerName;
    private String interviewerTitle;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
