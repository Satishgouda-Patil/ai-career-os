package com.ai.career.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private Long id;
    private Long emailId;
    private Long userId;
    private Long jobId;
    private Long recruiterId;
    private Integer version;
    private String subject;
    private String body;
    private String followup;
    private String linkedinMessage;
    private String status;
    private LocalDateTime createdAt;
}
