package com.ai.career.ai.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonOutputValidator {

    private final ObjectMapper objectMapper;

    public <T> T validateAndParse(String rawOutput, Class<T> targetClass) {
        if (rawOutput == null || rawOutput.isBlank()) {
            throw new IllegalArgumentException("Raw LLM output is empty or null.");
        }

        try {
            String cleanedJson = extractJsonPayload(rawOutput);
            return objectMapper.readValue(cleanedJson, targetClass);
        } catch (Exception e) {
            log.error("Failed to parse and validate LLM output for class {}: {}", targetClass.getSimpleName(), e.getMessage());
            throw new IllegalArgumentException("Invalid LLM JSON response payload: " + e.getMessage(), e);
        }
    }

    private String extractJsonPayload(String raw) {
        String trimmed = raw.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }
}
