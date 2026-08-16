package com.ai.career.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordActivityRequest {
    private String activityType;
    private String source;
    private String description;
    private Map<String, Object> metadata;
    private Double confidence;
}
