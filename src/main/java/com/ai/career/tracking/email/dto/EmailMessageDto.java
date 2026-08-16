package com.ai.career.tracking.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {
    private Long id;
    private Long userId;
    private String provider;
    private String externalMessageId;
    private String externalThreadId;
    private String sender;
    private String senderDomain;
    private String subject;
    private String bodySnippet;
    private LocalDateTime receivedAt;
    private String classification;
    private Double classificationConfidence;
    private Long applicationId;
    private LocalDateTime processedAt;
}
