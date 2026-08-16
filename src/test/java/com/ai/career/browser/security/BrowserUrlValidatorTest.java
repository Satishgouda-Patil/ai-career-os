package com.ai.career.browser.security;

import com.ai.career.browser.config.BrowserProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrowserUrlValidatorTest {

    private BrowserProperties browserProperties;
    private BrowserUrlValidator validator;

    @BeforeEach
    void setUp() {
        browserProperties = new BrowserProperties();
        browserProperties.setAllowLocalhost(false);
        validator = new BrowserUrlValidator(browserProperties);
    }

    @Test
    void testValidPublicUrls() {
        assertDoesNotThrow(() -> validator.validateUrl("https://example.com/job/123"));
        assertDoesNotThrow(() -> validator.validateUrl("http://careers.company.org/apply"));
    }

    @Test
    void testForbiddenSchemes() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("data:text/html,<h1>Hello</h1>"));
    }

    @Test
    void testSsrfProtectionLocalhost() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("http://localhost:8080/test"));
        assertThrows(IllegalArgumentException.class, () -> validator.validateUrl("http://127.0.0.1:8080/test"));
    }

    @Test
    void testAllowLocalhostWhenConfigured() {
        browserProperties.setAllowLocalhost(true);
        assertDoesNotThrow(() -> validator.validateUrl("http://localhost:8080/test"));
        assertDoesNotThrow(() -> validator.validateUrl("http://127.0.0.1:8080/test"));
    }
}
