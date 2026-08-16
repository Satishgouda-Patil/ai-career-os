package com.ai.career.integration.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ProductionProviderRegistry {

    public enum ProviderCategory {
        JOB_DISCOVERY,
        EMAIL_INTELLIGENCE,
        APPLICATION_EXECUTION,
        NOTIFICATIONS
    }

    public interface ProviderPlugin {
        String getProviderName();
        ProviderCategory getCategory();
        boolean isSandbox();
    }

    private final Map<String, ProviderPlugin> registry = new ConcurrentHashMap<>();

    public void registerProvider(ProviderPlugin plugin) {
        log.info("Registering provider plugin: {} (Category: {}, Sandbox: {})",
            plugin.getProviderName(), plugin.getCategory(), plugin.isSandbox());
        registry.put(plugin.getProviderName().toUpperCase(), plugin);
    }

    public Optional<ProviderPlugin> getProvider(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(registry.get(name.toUpperCase()));
    }

    public Map<String, ProviderPlugin> getAllProviders() {
        return Map.copyOf(registry);
    }
}
