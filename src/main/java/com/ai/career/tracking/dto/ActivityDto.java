package com.ai.career.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
    private Long id;
    private Long applicationId;
    private String activityType;
    private String source;
    private String description;
    private Map<String, Object> metadata;
    private Double confidence;
    private LocalDateTime createdAt;
}
