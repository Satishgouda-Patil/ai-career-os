package com.ai.career.tracking.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulateEmailRequest {
    private String sender;
    private String subject;
    private String bodySnippet;
    private String externalThreadId;
    private Long applicationId;
}
