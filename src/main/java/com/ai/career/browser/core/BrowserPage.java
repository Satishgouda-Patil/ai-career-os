package com.ai.career.browser.core;

import java.util.List;
import java.util.Map;

/**
 * Provider-agnostic read-only page interface for DOM inspection.
 */
public interface BrowserPage {

    String url();

    String title();

    String content();

    Object evaluate(String expression);

    ElementHandle querySelector(String selector);

    List<ElementHandle> querySelectorAll(String selector);

    interface ElementHandle {
        String getAttribute(String name);

        String getTextContent();

        String getTagName();

        boolean isVisible();

        boolean isEnabled();

        boolean isChecked();

        List<ElementHandle> querySelectorAll(String selector);

        ElementHandle querySelector(String selector);

        String evaluateString(String expression);
    }
}
