package com.ai.career.browser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.browser")
public class BrowserProperties {

    /**
     * Master switch for browser operations. Disabled by default for safety.
     */
    private boolean enabled = false;

    /**
     * Whether to run in headless mode. Defaults to true.
     */
    private boolean headless = true;

    /**
     * General timeout in milliseconds. Defaults to 30000ms (30s).
     */
    private long timeoutMs = 30000;

    /**
     * Navigation timeout in milliseconds. Defaults to 30000ms (30s).
     */
    private long navigationTimeoutMs = 30000;

    /**
     * Maximum concurrent pages allowed. Defaults to 1.
     */
    private int maxPages = 1;

    /**
     * Flag to allow localhost and private network addresses during local testing.
     */
    private boolean allowLocalhost = false;
}
