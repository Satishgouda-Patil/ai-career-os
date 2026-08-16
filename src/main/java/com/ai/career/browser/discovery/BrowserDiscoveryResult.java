package com.ai.career.browser.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowserDiscoveryResult {
    private String url;
    private String finalUrl;
    private String title;
    private boolean redirected;
    private boolean captchaDetected;
    private boolean loginRequired;

    @Builder.Default
    private Instant discoveredAt = Instant.now();

    @Builder.Default
    private List<DiscoveredForm> forms = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
