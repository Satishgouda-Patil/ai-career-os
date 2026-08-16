package com.ai.career.browser.security;

import com.ai.career.browser.config.BrowserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

@Component
@RequiredArgsConstructor
public class BrowserUrlValidator {

    private final BrowserProperties browserProperties;

    public void validateUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        String trimmed = urlString.trim();
        String lower = trimmed.toLowerCase();

        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) {
            throw new IllegalArgumentException("URL scheme is forbidden: " + urlString);
        }

        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Only HTTP and HTTPS protocols are allowed: " + urlString);
            }

            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("Invalid host in URL: " + urlString);
            }

            if (browserProperties.isAllowLocalhost()) {
                // Testing mode allows localhost and private network targets
                return;
            }

            // SSRF Check: IP / Host resolution check
            if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("::1") || host.equals("0.0.0.0")) {
                throw new IllegalArgumentException("Access to local address is forbidden: " + host);
            }

            try {
                InetAddress inetAddress = InetAddress.getByName(host);
                if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isAnyLocalAddress() || inetAddress.isLinkLocalAddress()) {
                    throw new IllegalArgumentException("Access to private/local network IP address is forbidden: " + host);
                }
            } catch (java.net.UnknownHostException ignored) {
                // Host cannot be resolved via DNS locally; URL structure is valid.
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or unsafe URL: " + urlString + " (" + e.getMessage() + ")", e);
        }
    }
}
