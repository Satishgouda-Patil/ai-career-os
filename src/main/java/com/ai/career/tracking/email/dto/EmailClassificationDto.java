package com.ai.career.tracking.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailClassificationDto {
    private String classification;
    private Double confidence;
    private Map<String, Object> extractedData;
    private String model;
}
