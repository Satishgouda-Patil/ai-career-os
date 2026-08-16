package com.ai.career.job.connector;

import com.ai.career.integration.domain.entity.IntegrationCredential;
import com.ai.career.integration.domain.repository.IntegrationCredentialRepository;
import com.ai.career.integration.registry.ProductionProviderRegistry;
import com.ai.career.integration.service.CredentialEncryptionService;
import com.ai.career.integration.service.IntegrationAuditService;
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
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ProductionJoobleJobFetcher implements JobFetcher, ProductionProviderRegistry.ProviderPlugin {

    private final RestClient restClient;
    private final CredentialEncryptionService encryptionService;
    private final IntegrationCredentialRepository credentialRepository;
    private final IntegrationAuditService auditService;
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    public ProductionJoobleJobFetcher(
        RestClient.Builder restClientBuilder,
        CredentialEncryptionService encryptionService,
        IntegrationCredentialRepository credentialRepository,
        IntegrationAuditService auditService,
        ProductionProviderRegistry registry,
        @Value("${app.job.jooble.base-url:https://jooble.org/api}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.encryptionService = encryptionService;
        this.credentialRepository = credentialRepository;
        this.auditService = auditService;
        registry.registerProvider(this);
    }

    @Override
    public String getSource() {
        return "JOOBLE_PRODUCTION";
    }

    @Override
    public String getProviderName() {
        return "JOOBLE_PRODUCTION";
    }

    @Override
    public ProductionProviderRegistry.ProviderCategory getCategory() {
        return ProductionProviderRegistry.ProviderCategory.JOB_DISCOVERY;
    }

    @Override
    public boolean isSandbox() {
        return false;
    }

    public synchronized void checkRateLimit() {
        long now = System.currentTimeMillis();
        while (!requestTimestamps.isEmpty() && now - requestTimestamps.peek() > 60000) {
            requestTimestamps.poll();
        }
        if (requestTimestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for Jooble Production Provider (10 req/min)");
            throw new IllegalStateException("Rate limit exceeded for Jooble Production Provider (max 10 req/min)");
        }
        requestTimestamps.add(now);
    }

    @Override
    public List<JobDto> fetchJobs(String keywords, String location) {
        checkRateLimit();
        long startTime = System.currentTimeMillis();

        // 1. Fetch encrypted credentials from vault (Fallback to API key if present)
        String apiKey = fetchEncryptedApiKey().orElse("mock-production-api-key");

        JoobleRequestDto request = JoobleRequestDto.builder()
            .keywords(keywords)
            .location(location)
            .page(1)
            .build();

        int maxRetries = 3;
        long backoffMs = 200;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                JoobleResponseDto response = restClient.post()
                    .uri("/" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JoobleResponseDto.class);

                long duration = System.currentTimeMillis() - startTime;
                auditService.recordAudit(
                    1L, null, getProviderName(), "FETCH_JOBS", "SUCCESS",
                    "keywords=" + keywords + "&location=" + location,
                    "fetched=" + (response != null && response.getJobs() != null ? response.getJobs().size() : 0),
                    duration, null
                );

                if (response == null || response.getJobs() == null) {
                    return Collections.emptyList();
                }

                return response.getJobs().stream()
                    .map(item -> JobDto.builder()
                        .source(getSource())
                        .sourceJobId(item.getId() != null ? item.getId().toString() : String.valueOf(item.hashCode()))
                        .title(item.getTitle())
                        .company(item.getCompany())
                        .location(item.getLocation())
                        .description(item.getSnippet())
                        .url(item.getLink())
                        .postedAt(LocalDateTime.now())
                        .build())
                    .toList();

            } catch (Exception e) {
                log.warn("Jooble Production API attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == maxRetries) {
                    long duration = System.currentTimeMillis() - startTime;
                    auditService.recordAudit(
                        1L, null, getProviderName(), "FETCH_JOBS", "FAILED",
                        "keywords=" + keywords + "&location=" + location,
                        "error=" + e.getMessage(),
                        duration, "HTTP_ERROR"
                    );
                    log.error("Jooble Production Provider failed safely without falling back to mock as fake success.");
                    throw new IllegalStateException("Production Jooble API failed: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(backoffMs);
                    backoffMs *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted during exponential backoff", ie);
                }
            }
        }

        return Collections.emptyList();
    }

    private Optional<String> fetchEncryptedApiKey() {
        return credentialRepository.findByUserIdAndProviderName(1L, getProviderName())
            .map(cred -> encryptionService.decrypt(cred.getEncryptedPayload(), cred.getPayloadIv()));
    }
}
