package com.ai.career.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteApplicationRequest {
    private boolean dryRun;
    private String overrideProviderName;
    private Map<String, String> answers;
}
