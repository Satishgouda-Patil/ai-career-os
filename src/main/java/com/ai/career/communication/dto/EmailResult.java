package com.ai.career.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResult {
    private String subject;
    private String body;
    private String followup;
    private String linkedinMessage;
}
