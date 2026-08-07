package com.ai.career.job.connector;

import com.ai.career.job.dto.JobDto;
import com.ai.career.job.dto.JoobleRequestDto;
import com.ai.career.job.dto.JoobleResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JoobleJobFetcher implements JobFetcher {

    private final RestClient restClient;
    private final String apiKey;

    public JoobleJobFetcher(
        RestClient.Builder restClientBuilder,
        @Value("${app.job.jooble.api-key:mock-api-key}") String apiKey,
        @Value("${app.job.jooble.base-url:https://jooble.org/api}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public String getSource() {
        return "JOOBLE";
    }

    @Override
    public List<JobDto> fetchJobs(String keywords, String location) {
        if ("mock-api-key".equalsIgnoreCase(apiKey)) {
            log.info("Jooble API key is unconfigured/mock. Returning mock tech jobs for testing.");
            return getMockJobs(keywords, location);
        }

        try {
            JoobleRequestDto request = JoobleRequestDto.builder()
                .keywords(keywords)
                .location(location)
                .page(1)
                .build();

            JoobleResponseDto response = restClient.post()
                .uri("/" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JoobleResponseDto.class);

            if (response == null || response.getJobs() == null) {
                return Collections.emptyList();
            }

            return response.getJobs().stream()
                .map(item -> JobDto.builder()
                    .source(getSource())
                    .sourceJobId(item.getId() != null ? item.getId() : String.valueOf(item.hashCode()))
                    .title(item.getTitle())
                    .company(item.getCompany() != null ? item.getCompany() : "Unknown Company")
                    .location(item.getLocation())
                    .description(item.getSnippet())
                    .url(item.getLink())
                    .postedAt(LocalDateTime.now())
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch jobs from Jooble API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<JobDto> getMockJobs(String keywords, String location) {
        return List.of(
            JobDto.builder()
                .source(getSource())
                .sourceJobId("jooble-mock-101")
                .title("Senior Java Spring Boot Engineer")
                .company("Acme AI Solutions")
                .location(location != null ? location : "Remote")
                .description("Looking for experienced Java 21, Spring Boot, MySQL, Redis, and RabbitMQ developer.")
                .url("https://example.com/jobs/101")
                .postedAt(LocalDateTime.now())
                .build(),
            JobDto.builder()
                .source(getSource())
                .sourceJobId("jooble-mock-102")
                .title("Backend Microservices Developer")
                .company("Global Cloud Systems")
                .location(location != null ? location : "San Francisco, CA")
                .description("Backend developer with expertise in Java, Docker, REST APIs, and microservices architecture.")
                .url("https://example.com/jobs/102")
                .postedAt(LocalDateTime.now())
                .build()
        );
    }
}
