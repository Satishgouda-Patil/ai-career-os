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
public class RawEmailMessageDto {
    private String provider;
    private String externalMessageId;
    private String externalThreadId;
    private String sender;
    private String subject;
    private String bodySnippet;
    private LocalDateTime receivedAt;
}
