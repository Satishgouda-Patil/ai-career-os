package com.ai.career.execution.browser;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.browser.core.BrowserPage;
import com.ai.career.browser.core.BrowserSession;
import com.ai.career.browser.core.BrowserSessionFactory;
import com.ai.career.execution.browser.dto.BrowserDiscoveryResultDto;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.service.IntegrationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowserDiscoveryService {

    private final BrowserSessionFactory sessionFactory;
    private final BrowserSafetyPolicy safetyPolicy;
    private final ApplicationRepository applicationRepository;
    private final DistributedExecutionLock distributedExecutionLock;
    private final IntegrationAuditService auditService;

    @Transactional(readOnly = true)
    public BrowserDiscoveryResultDto discoverForm(Long applicationId, Long userId) {
        long startTime = System.currentTimeMillis();

        // 1. Verify Application Ownership & State
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own application ID: " + applicationId);
        }

        String targetUrl = application.getJob() != null ? application.getJob().getUrl() : null;

        // 2. Safety Check: Target URL
        if (!safetyPolicy.isUrlSafe(targetUrl)) {
            log.warn("URL safety check failed for target: {}", targetUrl);
            recordAuditSafely(userId, applicationId, "DOMAIN_NOT_ALLOWED", "url=" + targetUrl);
            return BrowserDiscoveryResultDto.builder()
                .applicationId(applicationId)
                .status("DOMAIN_NOT_ALLOWED")
                .executionMode("READ_ONLY")
                .submissionAttempted(false)
                .targetUrl(targetUrl)
                .fields(List.of())
                .build();
        }

        // 3. Acquire Distributed Lock
        String lockKey = "application-browser-discovery:" + applicationId;
        if (!distributedExecutionLock.acquire(lockKey, "LOCK_DISCOVERY", 300)) {
            log.warn("Failed to acquire discovery lock for application ID: {}", applicationId);
            recordAuditSafely(userId, applicationId, "LOCK_NOT_ACQUIRED", "lockKey=" + lockKey);
            return BrowserDiscoveryResultDto.builder()
                .applicationId(applicationId)
                .status("LOCK_NOT_ACQUIRED")
                .executionMode("READ_ONLY")
                .submissionAttempted(false)
                .targetUrl(targetUrl)
                .fields(List.of())
                .build();
        }

        try (BrowserSession session = sessionFactory.createSession()) {
            log.info("Navigating browser session to: {}", targetUrl);
            session.navigate(targetUrl);

            BrowserPage page = session.getPage();
            String finalUrl = session.getCurrentUrl();

            if (!safetyPolicy.isRedirectSafe(targetUrl, finalUrl)) {
                log.warn("Unsafe redirect detected: {} -> {}", targetUrl, finalUrl);
                recordAuditSafely(userId, applicationId, "UNSAFE_REDIRECT_BLOCKED", "redirect=" + finalUrl);
                return BrowserDiscoveryResultDto.builder()
                    .applicationId(applicationId)
                    .status("UNSAFE_REDIRECT")
                    .executionMode("READ_ONLY")
                    .submissionAttempted(false)
                    .targetUrl(targetUrl)
                    .finalUrl(finalUrl)
                    .fields(List.of())
                    .build();
            }

            // 4. Read-Only Form & Field Discovery
            List<BrowserDiscoveryResultDto.DiscoveredFieldDto> discoveredFields = new ArrayList<>();
            List<BrowserPage.ElementHandle> formElements = page.querySelectorAll("form");
            int formsCount = formElements.isEmpty() ? 1 : formElements.size();

            List<BrowserPage.ElementHandle> inputs = page.querySelectorAll("input, select, textarea, button");
            for (BrowserPage.ElementHandle el : inputs) {
                String tagName = el.getTagName().toLowerCase();
                String inputType = el.getAttribute("type");
                String rawType = inputType != null ? inputType.toUpperCase() : tagName.toUpperCase();

                // Prohibit submission button clicks — record as discovered control only
                if ("SUBMIT".equals(rawType) || "BUTTON".equals(rawType)) {
                    String btnText = el.getTextContent();
                    if (btnText != null && (btnText.toLowerCase().contains("submit") || btnText.toLowerCase().contains("apply"))) {
                        log.info("Discovered submit control '{}' - Recording without executing/clicking.", btnText.trim());
                    }
                }

                String idAttr = el.getAttribute("id");
                String nameAttr = el.getAttribute("name");
                String placeholder = el.getAttribute("placeholder");
                String ariaLabel = el.getAttribute("aria-label");
                String acceptAttr = el.getAttribute("accept");

                boolean isRequired = el.getAttribute("required") != null || "true".equalsIgnoreCase(el.getAttribute("aria-required"));

                String label = ariaLabel != null ? ariaLabel : (nameAttr != null ? nameAttr : (placeholder != null ? placeholder : (idAttr != null ? idAttr : "UNKNOWN")));
                String normalizedLabel = label.replaceAll("[_-]", " ").toLowerCase().trim();

                String selector = idAttr != null && !idAttr.isBlank() ? "#" + idAttr : (nameAttr != null && !nameAttr.isBlank() ? "[name='" + nameAttr + "']" : tagName);

                discoveredFields.add(BrowserDiscoveryResultDto.DiscoveredFieldDto.builder()
                    .fieldSelector(selector)
                    .originalFieldId(idAttr)
                    .name(nameAttr)
                    .label(label)
                    .normalizedLabel(normalizedLabel)
                    .type(rawType)
                    .required(isRequired)
                    .accept(acceptAttr)
                    .build());
            }

            long duration = System.currentTimeMillis() - startTime;
            recordAuditSafely(userId, applicationId, "FORM_DISCOVERY_COMPLETED", "fieldsCount=" + discoveredFields.size());

            return BrowserDiscoveryResultDto.builder()
                .applicationId(applicationId)
                .status("DISCOVERED")
                .executionMode("READ_ONLY")
                .submissionAttempted(false)
                .targetUrl(targetUrl)
                .finalUrl(finalUrl)
                .formsDetected(formsCount)
                .fieldsDetected(discoveredFields.size())
                .fields(discoveredFields)
                .build();

        } catch (Exception e) {
            log.error("Error during browser form discovery for app ID: {}", applicationId, e);
            recordAuditSafely(userId, applicationId, "BROWSER_DISCOVERY_FAILED", "error=" + e.getMessage());
            return BrowserDiscoveryResultDto.builder()
                .applicationId(applicationId)
                .status("DISCOVERY_FAILED")
                .executionMode("READ_ONLY")
                .submissionAttempted(false)
                .targetUrl(targetUrl)
                .fields(List.of())
                .build();
        }
    }

    private void recordAuditSafely(Long userId, Long applicationId, String action, String summary) {
        try {
            auditService.recordAudit(userId, applicationId, "PLAYWRIGHT_READONLY_DISCOVERY", action, "SUCCESS", summary, "readOnly=true", 100L, null);
        } catch (Exception e) {
            log.warn("Failed to record audit log cleanly for browser discovery", e);
        }
    }
}
