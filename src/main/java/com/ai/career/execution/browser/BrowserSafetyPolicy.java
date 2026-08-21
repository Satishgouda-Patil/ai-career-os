package com.ai.career.execution.browser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Slf4j
@Component
public class BrowserSafetyPolicy {

    private static final Set<String> ALLOWED_DOMAINS = Set.of(
        "greenhouse.io",
        "boards.greenhouse.io",
        "localhost",
        "127.0.0.1"
    );

    public boolean isUrlSafe(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            log.warn("Target URL is null or blank");
            return false;
        }

        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                log.warn("Unsupported scheme in URL: {}", scheme);
                return false;
            }

            String host = uri.getHost();
            if (host == null) {
                log.warn("Null host in URL: {}", urlString);
                return false;
            }

            String lowerHost = host.toLowerCase();
            boolean domainAllowed = ALLOWED_DOMAINS.stream()
                .anyMatch(domain -> lowerHost.equals(domain) || lowerHost.endsWith("." + domain));

            if (!domainAllowed) {
                log.warn("Host '{}' is not in allowed domain policy", host);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("URL syntax validation failed for '{}': {}", urlString, e.getMessage());
            return false;
        }
    }

    public boolean isRedirectSafe(String originalUrl, String finalUrl) {
        if (!isUrlSafe(finalUrl)) {
            log.warn("Final redirect URL '{}' failed safety policy check", finalUrl);
            return false;
        }
        return true;
    }
}
