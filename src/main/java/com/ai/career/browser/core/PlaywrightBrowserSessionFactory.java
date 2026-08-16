package com.ai.career.browser.core;

import com.ai.career.browser.config.BrowserProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaywrightBrowserSessionFactory implements BrowserSessionFactory {

    private final BrowserProperties browserProperties;

    @Override
    public BrowserSession createSession() {
        if (!browserProperties.isEnabled()) {
            throw new IllegalStateException("Browser operations are disabled by configuration (app.browser.enabled=false)");
        }

        log.info("Launching Playwright Chromium browser (headless={})", browserProperties.isHeadless());
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(browserProperties.isHeadless()));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        return new PlaywrightBrowserSession(playwright, browser, context, page, browserProperties);
    }
}
