package com.ai.career.browser.interaction;

import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BrowserFormInteractor {

    public void fillText(BrowserSession session, String selector, String text) {
        log.info("Filling text field [{}] with value", selector);
        BrowserPage page = session.getPage();
        BrowserPage.ElementHandle element = page.querySelector(selector);
        if (element == null) {
            throw new IllegalArgumentException("FIELD_NOT_FOUND: " + selector);
        }

        // Focus & Fill
        page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (el) { el.focus(); el.value = ''; el.value = "
                + toJsonString(text) + "; el.dispatchEvent(new Event('input', {bubbles: true})); el.dispatchEvent(new Event('change', {bubbles: true})); } })()");

        // Read Back & Verify
        String readBack = element.evaluateString("el => el.value");
        if (text != null && !text.equals(readBack)) {
            throw new IllegalStateException("INTERACTION_VERIFICATION_FAILED: Expected '" + text + "' but read back '" + readBack + "' for " + selector);
        }
    }

    public void selectOption(BrowserSession session, String selector, String optionValue) {
        log.info("Selecting option [{}] for select [{}]", optionValue, selector);
        BrowserPage page = session.getPage();
        BrowserPage.ElementHandle element = page.querySelector(selector);
        if (element == null) {
            throw new IllegalArgumentException("FIELD_NOT_FOUND: " + selector);
        }

        // Check if option exists
        Object optionExists = page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (!el) return false; return Array.from(el.options).some(o => o.value === " + toJsonString(optionValue) + " || o.text.trim() === " + toJsonString(optionValue) + "); })()");
        if (Boolean.FALSE.equals(optionExists) || "false".equalsIgnoreCase(String.valueOf(optionExists))) {
            throw new IllegalArgumentException("OPTION_MISMATCH: Option '" + optionValue + "' not found in select " + selector);
        }

        // Select option
        page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (el) { for (let o of el.options) { if (o.value === "
                + toJsonString(optionValue) + " || o.text.trim() === " + toJsonString(optionValue) + ") { o.selected = true; } else { o.selected = false; } } el.dispatchEvent(new Event('change', {bubbles: true})); } })()");

        // Read Back & Verify
        String selectedVal = element.evaluateString("el => el.value");
        String selectedText = element.evaluateString("el => el.options[el.selectedIndex] ? el.options[el.selectedIndex].text.trim() : ''");

        if (!optionValue.equals(selectedVal) && !optionValue.equals(selectedText)) {
            throw new IllegalStateException("INTERACTION_VERIFICATION_FAILED: Selected option does not match '" + optionValue + "' for " + selector);
        }
    }

    public void checkCheckbox(BrowserSession session, String selector, boolean check) {
        log.info("Setting checkbox [{}] to {}", selector, check);
        BrowserPage page = session.getPage();
        BrowserPage.ElementHandle element = page.querySelector(selector);
        if (element == null) {
            throw new IllegalArgumentException("FIELD_NOT_FOUND: " + selector);
        }

        page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (el) { el.checked = " + check + "; el.dispatchEvent(new Event('change', {bubbles: true})); } })()");

        boolean isChecked = Boolean.TRUE.equals(Boolean.parseBoolean(element.evaluateString("el => el.checked")));
        if (isChecked != check) {
            throw new IllegalStateException("INTERACTION_VERIFICATION_FAILED: Checkbox state mismatch for " + selector);
        }
    }

    public void selectRadio(BrowserSession session, String selector, String value) {
        log.info("Selecting radio [{}] with value [{}]", selector, value);
        BrowserPage page = session.getPage();
        String radioSelector = selector + "[value='" + value + "']";
        BrowserPage.ElementHandle element = page.querySelector(radioSelector);
        if (element == null) {
            element = page.querySelector(selector);
        }
        if (element == null) {
            throw new IllegalArgumentException("FIELD_NOT_FOUND: " + selector);
        }

        page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (el) { el.checked = true; el.dispatchEvent(new Event('change', {bubbles: true})); } })()");

        boolean isChecked = Boolean.TRUE.equals(Boolean.parseBoolean(element.evaluateString("el => el.checked")));
        if (!isChecked) {
            throw new IllegalStateException("INTERACTION_VERIFICATION_FAILED: Radio selection failed for " + selector);
        }
    }

    public void uploadFile(BrowserSession session, String selector, String filePath) {
        log.info("Uploading file [{}] to field [{}]", filePath, selector);
        BrowserPage page = session.getPage();
        BrowserPage.ElementHandle element = page.querySelector(selector);
        if (element == null) {
            throw new IllegalArgumentException("FIELD_NOT_FOUND: " + selector);
        }

        // Verify local file exists
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("FILE_NOT_FOUND: Local file does not exist at " + filePath);
        }

        page.evaluate("(() => { let el = document.querySelector(" + toJsonString(selector) + "); if (el) { el.dispatchEvent(new Event('change', {bubbles: true})); } })()");
    }

    private String toJsonString(String val) {
        if (val == null) return "''";
        return "'" + val.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r") + "'";
    }
}
