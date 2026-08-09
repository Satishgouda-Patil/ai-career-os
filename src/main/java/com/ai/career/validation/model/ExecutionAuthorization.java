package com.ai.career.validation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionAuthorization {
    private Long applicationId;
    private Long authorizedByUserId;
    private LocalDateTime authorizedAt;
    private boolean executionAuthorized;
    private String note;
}
