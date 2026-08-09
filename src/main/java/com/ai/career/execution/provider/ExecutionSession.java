package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionSession {
    private String sessionId;
    private String providerType;
    private LocalDateTime startedAt;
    private boolean active;
}
