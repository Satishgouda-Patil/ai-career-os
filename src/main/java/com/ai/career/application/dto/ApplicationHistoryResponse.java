package com.ai.career.application.dto;

import com.ai.career.application.domain.entity.ApplicationState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationHistoryResponse {
    private Long id;
    private Long applicationId;
    private ApplicationState fromStatus;
    private ApplicationState toStatus;
    private String reason;
    private String triggerType;
    private String actorType;
    private Long actorId;
    private String correlationId;
    private LocalDateTime createdAt;
}
