package com.ai.career.browser.core;

/**
 * Provider-agnostic browser session lifecycle.
 */
public interface BrowserSession extends AutoCloseable {

    void navigate(String url);

    BrowserPage getPage();

    String getCurrentUrl();

    String getTitle();

    String getPageContent();

    @Override
    void close();
}
