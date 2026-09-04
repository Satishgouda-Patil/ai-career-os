package com.ai.career.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFingerprint {
    private String previewId;
    private String formFingerprint;
    private LocalDateTime createdAt;
    private Long applicationId;
    private String targetUrl;
    private String provider;
    private String fieldsHash;
}
