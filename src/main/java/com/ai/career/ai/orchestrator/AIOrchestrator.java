package com.ai.career.ai.orchestrator;

import com.ai.career.ai.context.ContextBuilder;
import com.ai.career.ai.prompt.PromptManager;
import com.ai.career.ai.validator.JsonOutputValidator;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.Profile;
import com.ai.career.llm.client.OllamaClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIOrchestrator {

    private final ContextBuilder contextBuilder;
    private final PromptManager promptManager;
    private final OllamaClientService ollamaClientService;
    private final JsonOutputValidator jsonOutputValidator;

    public <T> T executeAiPipeline(
        String promptName,
        Profile profile,
        Job job,
        Class<T> responseType,
        T fallbackObject
    ) {
        log.info("Starting AI Orchestrator Pipeline for prompt: {}", promptName);

        Map<String, String> context = contextBuilder.buildContext(profile, job);
        String prompt = promptManager.renderPrompt(promptName, context);

        if (!ollamaClientService.isAvailable()) {
            log.warn("Ollama LLM server offline. Returning configured fallback instance for {}", responseType.getSimpleName());
            return fallbackObject;
        }

        int maxRetries = 2;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String rawOutput = ollamaClientService.generateCompletion(prompt);
                if (rawOutput != null && !rawOutput.isBlank()) {
                    return jsonOutputValidator.validateAndParse(rawOutput, responseType);
                }
            } catch (Exception e) {
                log.warn("Attempt {} failed for AI pipeline {}: {}", attempt, promptName, e.getMessage());
            }
        }

        log.warn("All AI attempts failed for {}. Returning fallback object.", promptName);
        return fallbackObject;
    }
}
