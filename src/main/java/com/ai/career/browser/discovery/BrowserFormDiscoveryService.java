package com.ai.career.browser.discovery;

import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import com.ai.career.browser.core.BrowserSessionFactory;
import com.ai.career.browser.security.BrowserUrlValidator;
import com.ai.career.form.model.FieldType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowserFormDiscoveryService {

    private final BrowserUrlValidator urlValidator;
    private final BrowserSessionFactory sessionFactory;

    public BrowserDiscoveryResult discoverForms(String targetUrl) {
        urlValidator.validateUrl(targetUrl);

        BrowserDiscoveryResult.BrowserDiscoveryResultBuilder resultBuilder = BrowserDiscoveryResult.builder()
                .url(targetUrl)
                .discoveredAt(Instant.now());

        List<String> warnings = new ArrayList<>();
        List<DiscoveredForm> discoveredForms = new ArrayList<>();

        try (BrowserSession session = sessionFactory.createSession()) {
            session.navigate(targetUrl);

            String finalUrl = session.getCurrentUrl();
            String title = session.getTitle();
            String content = session.getPageContent();

            boolean redirected = !targetUrl.equalsIgnoreCase(finalUrl);
            resultBuilder.finalUrl(finalUrl).title(title).redirected(redirected);

            if (redirected) {
                warnings.add("Page redirected from " + targetUrl + " to " + finalUrl);
            }

            // CAPTCHA Detection
            if (isCaptchaPresent(content, session.getPage())) {
                log.warn("CAPTCHA detected at URL: {}", finalUrl);
                warnings.add("CAPTCHA_DETECTED");
                return resultBuilder
                        .captchaDetected(true)
                        .warnings(warnings)
                        .forms(discoveredForms)
                        .build();
            }

            // Login Detection
            if (isLoginRequired(content, session.getPage())) {
                log.warn("Login requirement detected at URL: {}", finalUrl);
                warnings.add("LOGIN_REQUIRED");
                return resultBuilder
                        .loginRequired(true)
                        .warnings(warnings)
                        .forms(discoveredForms)
                        .build();
            }

            // Inspect DOM for forms
            BrowserPage page = session.getPage();
            List<BrowserPage.ElementHandle> formHandles = page.querySelectorAll("form");

            if (formHandles.isEmpty()) {
                // Check if there are orphan inputs outside a <form> tag
                List<BrowserPage.ElementHandle> standaloneInputs = page.querySelectorAll("input, textarea, select");
                if (!standaloneInputs.isEmpty()) {
                    DiscoveredForm implicitForm = extractFormDetails(page, null, 1, standaloneInputs);
                    discoveredForms.add(implicitForm);
                }
            } else {
                int formIndex = 1;
                for (BrowserPage.ElementHandle formHandle : formHandles) {
                    List<BrowserPage.ElementHandle> inputs = formHandle.querySelectorAll("input, textarea, select");
                    DiscoveredForm form = extractFormDetails(page, formHandle, formIndex++, inputs);
                    discoveredForms.add(form);
                }
            }

            return resultBuilder
                    .forms(discoveredForms)
                    .warnings(warnings)
                    .build();
        } catch (Exception e) {
            log.error("Error during form discovery for URL {}: {}", targetUrl, e.getMessage(), e);
            warnings.add("DISCOVERY_FAILED: " + e.getMessage());
            return resultBuilder
                    .warnings(warnings)
                    .forms(discoveredForms)
                    .build();
        }
    }

    private boolean isCaptchaPresent(String content, BrowserPage page) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        if (lower.contains("g-recaptcha") || lower.contains("h-captcha") || lower.contains("cf-turnstile")
                || lower.contains("verify you are human") || lower.contains("captcha-container")) {
            return true;
        }
        return !page.querySelectorAll(".g-recaptcha, .h-captcha, iframe[src*='captcha']").isEmpty();
    }

    private boolean isLoginRequired(String content, BrowserPage page) {
        if (content == null) return false;
        List<BrowserPage.ElementHandle> passwordFields = page.querySelectorAll("input[type='password']");
        return !passwordFields.isEmpty();
    }

    private DiscoveredForm extractFormDetails(BrowserPage page, BrowserPage.ElementHandle formHandle, int formIndex, List<BrowserPage.ElementHandle> inputs) {
        String formId = (formHandle != null && formHandle.getAttribute("id") != null && !formHandle.getAttribute("id").isEmpty())
                ? formHandle.getAttribute("id")
                : "form-" + formIndex;

        String action = formHandle != null ? formHandle.getAttribute("action") : "";
        String method = formHandle != null ? formHandle.getAttribute("method") : "GET";
        if (method == null || method.trim().isEmpty()) {
            method = "GET";
        }

        List<DiscoveredField> fields = new ArrayList<>();
        List<String> buttons = new ArrayList<>();

        for (BrowserPage.ElementHandle input : inputs) {
            String tagName = input.getTagName();
            String type = input.getAttribute("type") != null ? input.getAttribute("type").toLowerCase() : "text";

            if ("input".equalsIgnoreCase(tagName) && ("submit".equalsIgnoreCase(type) || "button".equalsIgnoreCase(type) || "image".equalsIgnoreCase(type))) {
                String btnVal = input.getAttribute("value") != null ? input.getAttribute("value") : input.getAttribute("name");
                buttons.add(btnVal != null ? btnVal : "Button");
                continue;
            }

            DiscoveredField field = extractFieldDetails(page, input);
            fields.add(field);
        }

        if (formHandle != null) {
            List<BrowserPage.ElementHandle> btnHandles = formHandle.querySelectorAll("button");
            for (BrowserPage.ElementHandle btn : btnHandles) {
                buttons.add(btn.getTextContent() != null ? btn.getTextContent().trim() : "Button");
            }
        }

        return DiscoveredForm.builder()
                .id(formId)
                .action(action != null ? action : "")
                .method(method.toUpperCase())
                .fields(fields)
                .buttons(buttons)
                .build();
    }

    private DiscoveredField extractFieldDetails(BrowserPage page, BrowserPage.ElementHandle element) {
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String tag = element.getTagName();
        String rawType = element.getAttribute("type") != null ? element.getAttribute("type").toLowerCase() : "";
        String placeholder = element.getAttribute("placeholder");
        String ariaLabel = element.getAttribute("aria-label");

        // Required check
        boolean required = element.getAttribute("required") != null
                || "true".equalsIgnoreCase(element.getAttribute("aria-required"));
        String requiredSource = required ? (element.getAttribute("required") != null ? "ATTRIBUTE_REQUIRED" : "ARIA_REQUIRED") : "NONE";

        boolean disabled = element.getAttribute("disabled") != null;
        boolean readonly = element.getAttribute("readonly") != null;

        // Label Resolution
        LabelResolution labelRes = resolveLabel(page, element, id, name, placeholder, ariaLabel);

        // Field Type Normalization
        FieldType fieldType = normalizeFieldType(tag, rawType);

        // Selector construction
        String selector = buildSelector(id, name, tag, rawType);

        // Options extraction
        List<DiscoveredOption> options = extractOptions(element, tag, rawType);

        return DiscoveredField.builder()
                .id(id != null ? id : "")
                .name(name != null ? name : "")
                .tag(tag)
                .type(rawType)
                .fieldType(fieldType)
                .label(labelRes.label)
                .labelSource(labelRes.source)
                .placeholder(placeholder != null ? placeholder : "")
                .required(required)
                .requiredSource(requiredSource)
                .disabled(disabled)
                .readonly(readonly)
                .selector(selector)
                .ariaLabel(ariaLabel != null ? ariaLabel : "")
                .options(options)
                .build();
    }

    private record LabelResolution(String label, String source) {}

    private LabelResolution resolveLabel(BrowserPage page, BrowserPage.ElementHandle element, String id, String name, String placeholder, String ariaLabel) {
        // 1. Explicit <label for="id">
        if (id != null && !id.isEmpty()) {
            List<BrowserPage.ElementHandle> labels = page.querySelectorAll("label[for='" + id + "']");
            if (!labels.isEmpty() && labels.get(0).getTextContent() != null && !labels.get(0).getTextContent().trim().isEmpty()) {
                return new LabelResolution(labels.get(0).getTextContent().trim(), "EXPLICIT_LABEL");
            }
        }

        // 2. Wrapping <label>
        String wrappingLabelText = element.evaluateString("el => { let parent = el.closest('label'); return parent ? parent.innerText : null; }");
        if (wrappingLabelText != null && !wrappingLabelText.trim().isEmpty()) {
            return new LabelResolution(wrappingLabelText.trim(), "WRAPPING_LABEL");
        }

        // 3. aria-label
        if (ariaLabel != null && !ariaLabel.trim().isEmpty()) {
            return new LabelResolution(ariaLabel.trim(), "ARIA_LABEL");
        }

        // 4. aria-labelledby
        String ariaLabelledBy = element.getAttribute("aria-labelledby");
        if (ariaLabelledBy != null && !ariaLabelledBy.isEmpty()) {
            BrowserPage.ElementHandle labelTarget = page.querySelector("#" + ariaLabelledBy);
            if (labelTarget != null && labelTarget.getTextContent() != null && !labelTarget.getTextContent().trim().isEmpty()) {
                return new LabelResolution(labelTarget.getTextContent().trim(), "ARIA_LABELLEDBY");
            }
        }

        // 5. placeholder
        if (placeholder != null && !placeholder.trim().isEmpty()) {
            return new LabelResolution(placeholder.trim(), "PLACEHOLDER");
        }

        // 6. Nearby descriptive text (sibling label/span)
        String nearbyText = element.evaluateString("el => { let prev = el.previousElementSibling; return (prev && prev.innerText) ? prev.innerText : null; }");
        if (nearbyText != null && !nearbyText.trim().isEmpty()) {
            return new LabelResolution(nearbyText.trim(), "NEARBY_TEXT");
        }

        // 7. Fallback to name/id
        if (name != null && !name.isEmpty()) {
            return new LabelResolution(name, "FIELD_NAME_FALLBACK");
        }
        if (id != null && !id.isEmpty()) {
            return new LabelResolution(id, "FIELD_ID_FALLBACK");
        }

        return new LabelResolution("Unlabelled Field", "UNRESOLVED");
    }

    private FieldType normalizeFieldType(String tag, String rawType) {
        if ("textarea".equalsIgnoreCase(tag)) {
            return FieldType.TEXTAREA;
        }
        if ("select".equalsIgnoreCase(tag)) {
            return FieldType.SELECT;
        }
        if ("input".equalsIgnoreCase(tag)) {
            return switch (rawType) {
                case "email" -> FieldType.EMAIL;
                case "tel", "phone" -> FieldType.PHONE;
                case "url" -> FieldType.URL;
                case "file" -> FieldType.FILE;
                case "number" -> FieldType.NUMBER;
                case "date" -> FieldType.DATE;
                case "radio" -> FieldType.RADIO;
                case "checkbox" -> FieldType.CHECKBOX;
                case "text", "search", "" -> FieldType.TEXT;
                default -> FieldType.UNKNOWN;
            };
        }
        return FieldType.UNKNOWN;
    }

    private String buildSelector(String id, String name, String tag, String rawType) {
        if (id != null && !id.isEmpty()) {
            return "#" + id;
        }
        if (name != null && !name.isEmpty()) {
            return tag + "[name='" + name + "']";
        }
        return tag + (rawType.isEmpty() ? "" : "[type='" + rawType + "']");
    }

    private List<DiscoveredOption> extractOptions(BrowserPage.ElementHandle element, String tag, String rawType) {
        List<DiscoveredOption> list = new ArrayList<>();
        if ("select".equalsIgnoreCase(tag)) {
            List<BrowserPage.ElementHandle> optionElements = element.querySelectorAll("option");
            for (BrowserPage.ElementHandle opt : optionElements) {
                list.add(DiscoveredOption.builder()
                        .value(opt.getAttribute("value") != null ? opt.getAttribute("value") : "")
                        .text(opt.getTextContent() != null ? opt.getTextContent().trim() : "")
                        .selected(opt.getAttribute("selected") != null)
                        .disabled(opt.getAttribute("disabled") != null)
                        .build());
            }
        }
        return list;
    }
}
