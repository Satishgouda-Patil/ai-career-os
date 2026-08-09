package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormResult {
    private boolean success;
    private Map<String, String> mappedAnswers;
    private List<String> unmappedFields;
    private String errorMessage;
}
