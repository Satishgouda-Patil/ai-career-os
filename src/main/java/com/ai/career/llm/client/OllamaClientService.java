package com.ai.career.llm.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OllamaClientService {

    private final RestClient restClient;
    private final String model;

    public OllamaClientService(
        @Qualifier("ollamaRestClient") RestClient restClient,
        @Value("${app.ollama.model:llama3}") String model
    ) {
        this.restClient = restClient;
        this.model = model;
    }

    public String generateCompletion(String prompt) {
        try {
            OllamaGenerateRequest request = OllamaGenerateRequest.builder()
                .model(model)
                .prompt(prompt)
                .stream(false)
                .build();

            OllamaGenerateResponse response = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OllamaGenerateResponse.class);

            if (response != null && response.getResponse() != null) {
                return response.getResponse().trim();
            }
        } catch (Exception e) {
            log.warn("Failed to communicate with local Ollama server: {}", e.getMessage());
        }
        return null;
    }

    public boolean isAvailable() {
        try {
            restClient.get().uri("/api/tags").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaGenerateRequest {
        private String model;
        private String prompt;
        private boolean stream;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaGenerateResponse {
        private String response;
        private boolean done;
    }
}
