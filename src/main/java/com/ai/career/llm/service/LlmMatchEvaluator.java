package com.ai.career.llm.service;

import com.ai.career.llm.client.OllamaClientService;
import com.ai.career.llm.prompt.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmMatchEvaluator {

    private final OllamaClientService ollamaClientService;

    public Integer evaluateMatchWithLlm(List<String> userSkills, String jobTitle, String jobDescription) {
        if (!ollamaClientService.isAvailable()) {
            log.debug("Ollama local LLM is offline. Fallback to keyword matching.");
            return null;
        }

        try {
            String prompt = String.format(
                PromptTemplates.MATCH_SCORING_PROMPT,
                String.join(", ", userSkills),
                jobTitle != null ? jobTitle : "",
                jobDescription != null ? jobDescription : ""
            );

            String response = ollamaClientService.generateCompletion(prompt);
            if (response != null) {
                String cleanNumber = response.replaceAll("[^0-9]", "");
                if (!cleanNumber.isEmpty()) {
                    int score = Integer.parseInt(cleanNumber);
                    return Math.min(100, Math.max(0, score));
                }
            }
        } catch (Exception e) {
            log.warn("Error during LLM match evaluation: {}", e.getMessage());
        }
        return null;
    }
}
