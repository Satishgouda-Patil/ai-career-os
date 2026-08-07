package com.ai.career.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PromptManager {

    private final ResourceLoader resourceLoader;
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    public PromptManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getPromptTemplate(String promptName) {
        return promptCache.computeIfAbsent(promptName, name -> {
            try {
                Resource resource = resourceLoader.getResource("classpath:prompts/" + name + ".txt");
                if (!resource.exists()) {
                    log.warn("Prompt resource classpath:prompts/{}.txt does not exist. Using fallback template.", name);
                    return getDefaultFallbackPrompt(name);
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.error("Failed to load prompt template: {}", name, e);
                return getDefaultFallbackPrompt(name);
            }
        });
    }

    public String renderPrompt(String promptName, Map<String, String> variables) {
        String template = getPromptTemplate(promptName);
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }

    private String getDefaultFallbackPrompt(String name) {
        return switch (name) {
            case "resume-generation" -> """
                Generate a structured ATS-optimized resume JSON for candidate: ${fullName} applying for job: ${jobTitle}.
                Candidate Skills: ${skills}. Summary: ${summary}. Job Description: ${jobDescription}.
                Return JSON only.
                """;
            case "ats-analysis" -> """
                Analyze the candidate resume against job requirements.
                Candidate Skills: ${skills}. Job Description: ${jobDescription}.
                Return JSON containing overallScore, keywordScore, formatScore, missingKeywords.
                """;
            default -> "Provide analysis for task " + name + " with variables ${variables}. Return JSON.";
        };
    }
}
