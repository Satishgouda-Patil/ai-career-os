package com.ai.career.browser.core;

import com.ai.career.browser.config.BrowserProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PlaywrightBrowserSession implements BrowserSession {

    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final BrowserProperties properties;

    public PlaywrightBrowserSession(Playwright playwright, Browser browser, BrowserContext context, Page page, BrowserProperties properties) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
        this.properties = properties;
        this.page.setDefaultTimeout(properties.getTimeoutMs());
        this.page.setDefaultNavigationTimeout(properties.getNavigationTimeoutMs());
    }

    @Override
    public void navigate(String url) {
        log.info("Navigating to URL: {}", url);
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @Override
    public BrowserPage getPage() {
        return new PlaywrightPageAdapter(page);
    }

    @Override
    public String getCurrentUrl() {
        return page.url();
    }

    @Override
    public String getTitle() {
        return page.title();
    }

    @Override
    public String getPageContent() {
        return page.content();
    }

    @Override
    public void close() {
        try {
            if (page != null && !page.isClosed()) {
                page.close();
            }
        } catch (Exception e) {
            log.warn("Error closing page: {}", e.getMessage());
        }
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser context: {}", e.getMessage());
        }
        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser: {}", e.getMessage());
        }
        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            log.warn("Error closing Playwright instance: {}", e.getMessage());
        }
        log.info("Playwright browser session closed cleanly.");
    }

    private static class PlaywrightPageAdapter implements BrowserPage {
        private final Page pwPage;

        public PlaywrightPageAdapter(Page pwPage) {
            this.pwPage = pwPage;
        }

        @Override
        public String url() {
            return pwPage.url();
        }

        @Override
        public String title() {
            return pwPage.title();
        }

        @Override
        public String content() {
            return pwPage.content();
        }

        @Override
        public Object evaluate(String expression) {
            return pwPage.evaluate(expression);
        }

        @Override
        public ElementHandle querySelector(String selector) {
            com.microsoft.playwright.ElementHandle handle = pwPage.querySelector(selector);
            return handle == null ? null : new PlaywrightElementHandleAdapter(handle);
        }

        @Override
        public List<ElementHandle> querySelectorAll(String selector) {
            List<com.microsoft.playwright.ElementHandle> handles = pwPage.querySelectorAll(selector);
            List<ElementHandle> list = new ArrayList<>();
            for (com.microsoft.playwright.ElementHandle h : handles) {
                list.add(new PlaywrightElementHandleAdapter(h));
            }
            return list;
        }
    }

    private static class PlaywrightElementHandleAdapter implements BrowserPage.ElementHandle {
        private final com.microsoft.playwright.ElementHandle pwElement;

        public PlaywrightElementHandleAdapter(com.microsoft.playwright.ElementHandle pwElement) {
            this.pwElement = pwElement;
        }

        @Override
        public String getAttribute(String name) {
            return pwElement.getAttribute(name);
        }

        @Override
        public String getTextContent() {
            return pwElement.textContent();
        }

        @Override
        public String getTagName() {
            Object res = pwElement.evaluate("el => el.tagName");
            return res != null ? res.toString().toLowerCase() : "";
        }

        @Override
        public boolean isVisible() {
            return pwElement.isVisible();
        }

        @Override
        public boolean isEnabled() {
            return pwElement.isEnabled();
        }

        @Override
        public boolean isChecked() {
            return pwElement.isChecked();
        }

        @Override
        public BrowserPage.ElementHandle querySelector(String selector) {
            com.microsoft.playwright.ElementHandle handle = pwElement.querySelector(selector);
            return handle == null ? null : new PlaywrightElementHandleAdapter(handle);
        }

        @Override
        public List<BrowserPage.ElementHandle> querySelectorAll(String selector) {
            List<com.microsoft.playwright.ElementHandle> handles = pwElement.querySelectorAll(selector);
            List<BrowserPage.ElementHandle> list = new ArrayList<>();
            for (com.microsoft.playwright.ElementHandle h : handles) {
                list.add(new PlaywrightElementHandleAdapter(h));
            }
            return list;
        }

        @Override
        public String evaluateString(String expression) {
            Object res = pwElement.evaluate(expression);
            return res != null ? res.toString() : null;
        }
    }
}
