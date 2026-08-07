package com.ai.career.coverletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterResponse {
    private Long id;
    private Long coverLetterId;
    private Long userId;
    private Long jobId;
    private Integer version;
    private String tone;
    private String status;
    private String content;
    private LocalDateTime createdAt;
}
