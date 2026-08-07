package com.ai.career.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String templateName;
    private Integer version;
    private String status;
    private String contentJson;
    private String pdfUrl;
    private String docxUrl;
    private LocalDateTime createdAt;
    private AtsAnalysisResponse analysis;
}
