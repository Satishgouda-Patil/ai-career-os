package com.ai.career.execution.gate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyGateResult {
    private boolean passed;

    @Builder.Default
    private List<String> failures = new ArrayList<>();

    private String primaryErrorCode;

    public static SafetyGateResult success() {
        return SafetyGateResult.builder()
                .passed(true)
                .failures(List.of())
                .build();
    }

    public static SafetyGateResult failure(List<String> failures, String errorCode) {
        return SafetyGateResult.builder()
                .passed(false)
                .failures(failures)
                .primaryErrorCode(errorCode)
                .build();
    }

    public static SafetyGateResult failure(String failureReason, String errorCode) {
        return SafetyGateResult.builder()
                .passed(false)
                .failures(List.of(failureReason))
                .primaryErrorCode(errorCode)
                .build();
    }
}
