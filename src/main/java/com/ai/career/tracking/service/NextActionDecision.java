package com.ai.career.tracking.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextActionDecision {
    private String nextAction;
    private LocalDateTime dueDate;
    private String reason;
    private String urgency; // LOW, MEDIUM, HIGH, CRITICAL
}
