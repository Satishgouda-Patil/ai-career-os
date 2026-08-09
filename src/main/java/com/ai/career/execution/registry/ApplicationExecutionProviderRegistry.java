package com.ai.career.execution.registry;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.execution.provider.ApplicationExecutionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationExecutionProviderRegistry {

    private final List<ApplicationExecutionProvider> providers;

    public ApplicationExecutionProvider resolve(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null when resolving execution provider");
        }

        Optional<ApplicationExecutionProvider> matchingProvider = providers.stream()
            .filter(provider -> provider.supports(application))
            .findFirst();

        if (matchingProvider.isEmpty()) {
            log.warn("No suitable ApplicationExecutionProvider found for Application ID: {}, Job URL: {}", application.getId(), application.getApplicationUrl());
            throw new UnsupportedOperationException("No execution provider available supporting Application ID: " + application.getId());
        }

        ApplicationExecutionProvider provider = matchingProvider.get();
        log.info("Resolved ApplicationExecutionProvider [{}] for Application ID: {}", provider.getProviderName(), application.getId());
        return provider;
    }

    public List<ApplicationExecutionProvider> getAllProviders() {
        return providers;
    }
}
