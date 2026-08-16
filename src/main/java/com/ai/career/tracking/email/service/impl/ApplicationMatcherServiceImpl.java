package com.ai.career.tracking.email.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.tracking.email.service.ApplicationMatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationMatcherServiceImpl implements ApplicationMatcherService {

    private final ApplicationRepository applicationRepository;

    private static final Pattern APP_ID_PATTERN = Pattern.compile("(?:app(?:lication)?\\s*(?:id|#)?\\s*:?\\s*)(\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    public Optional<ApplicationMatchResult> matchApplication(Long userId, String sender, String subject, String bodySnippet, String externalThreadId) {
        if (userId == null) {
            return Optional.empty();
        }

        List<Application> userApplications = applicationRepository.findByUserId(userId);
        if (userApplications.isEmpty()) {
            return Optional.empty();
        }

        String fullText = ((subject != null ? subject : "") + " " + (bodySnippet != null ? bodySnippet : "")).toLowerCase();
        String senderLower = sender != null ? sender.toLowerCase() : "";

        // 1. Check for explicit Application ID pattern match
        Matcher appRefMatcher = APP_ID_PATTERN.matcher(fullText);
        if (appRefMatcher.find()) {
            try {
                Long extractedAppId = Long.parseLong(appRefMatcher.group(1));
                Optional<Application> explicitMatch = userApplications.stream()
                        .filter(app -> app.getId().equals(extractedAppId))
                        .findFirst();
                if (explicitMatch.isPresent()) {
                    log.info("Explicit Application ID match found: App ID {}", extractedAppId);
                    return Optional.of(new ApplicationMatchResult(explicitMatch.get(), 0.99, "Explicit Application ID in email text"));
                }
            } catch (NumberFormatException ignored) {}
        }

        // 2. Match by Company Name & Job Title
        Application bestMatch = null;
        double bestScore = 0.0;
        String bestReason = "";

        for (Application app : userApplications) {
            if (app.getJob() == null) continue;

            String company = app.getJob().getCompany() != null ? app.getJob().getCompany().toLowerCase() : "";
            String title = app.getJob().getTitle() != null ? app.getJob().getTitle().toLowerCase() : "";

            boolean companyMatch = !company.isBlank() && (fullText.contains(company) || senderLower.contains(company.replaceAll("\\s+", "")));
            boolean titleMatch = !title.isBlank() && fullText.contains(title);

            double score = 0.0;
            String reason = "";

            if (companyMatch && titleMatch) {
                score = 0.90;
                reason = "Matched Company '" + company + "' and Job Title '" + title + "'";
            } else if (companyMatch) {
                score = 0.75;
                reason = "Matched Company '" + company + "'";
            } else if (titleMatch) {
                score = 0.60;
                reason = "Matched Job Title '" + title + "'";
            }

            if (score > bestScore) {
                bestScore = score;
                bestMatch = app;
                bestReason = reason;
            }
        }

        if (bestMatch != null && bestScore >= 0.70) {
            log.info("Confidence match found: Application ID {} with score {}", bestMatch.getId(), bestScore);
            return Optional.of(new ApplicationMatchResult(bestMatch, bestScore, bestReason));
        }

        log.info("No high-confidence application match found for email from sender {}", sender);
        return Optional.empty();
    }
}
