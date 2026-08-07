package com.ai.career.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaConfig {

    @Value("${app.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Bean
    public RestClient ollamaRestClient(RestClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}
