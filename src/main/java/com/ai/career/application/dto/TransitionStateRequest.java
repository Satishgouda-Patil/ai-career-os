package com.ai.career.application.dto;

import com.ai.career.application.domain.entity.ApplicationState;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionStateRequest {
    @NotNull(message = "Target state is required")
    private ApplicationState targetState;

    private String reason;
    private String triggerType;
    private String correlationId;
}
