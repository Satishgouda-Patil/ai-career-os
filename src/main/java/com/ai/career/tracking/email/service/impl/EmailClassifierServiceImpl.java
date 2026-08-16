package com.ai.career.tracking.email.service.impl;

import com.ai.career.tracking.email.dto.EmailClassificationDto;
import com.ai.career.tracking.email.service.EmailClassifierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class EmailClassifierServiceImpl implements EmailClassifierService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    @Override
    public EmailClassificationDto classify(String sender, String subject, String bodySnippet) {
        String fullText = ((subject != null ? subject : "") + " " + (bodySnippet != null ? bodySnippet : "")).toLowerCase();
        Map<String, Object> extractedData = new HashMap<>();

        // Extract meeting URL if present
        if (bodySnippet != null) {
            Matcher matcher = URL_PATTERN.matcher(bodySnippet);
            if (matcher.find()) {
                String url = matcher.group(0);
                if (url.contains("zoom.us") || url.contains("meet.google") || url.contains("teams.microsoft") || url.contains("calendly")) {
                    extractedData.put("meetingUrl", url);
                }
            }
        }

        // 1. REJECTION
        if (containsAny(fullText, "regret to inform", "pursuing other candidates", "moved forward with another", "not selected", "unable to offer", "not moving forward")) {
            return EmailClassificationDto.builder()
                    .classification("REJECTION")
                    .confidence(0.95)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        // 2. INTERVIEW_INVITATION
        if (containsAny(fullText, "interview", "schedule a call", "invitation to interview", "speak with our team", "calendar invite", "availability for a chat")) {
            extractedData.put("isInterviewInvite", true);
            return EmailClassificationDto.builder()
                    .classification("INTERVIEW_INVITATION")
                    .confidence(0.92)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        // 3. OFFER
        if (containsAny(fullText, "job offer", "offer letter", "pleased to offer you", "congratulations on your offer")) {
            return EmailClassificationDto.builder()
                    .classification("OFFER")
                    .confidence(0.98)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        // 4. ASSESSMENT
        if (containsAny(fullText, "coding assessment", "hackerrank", "codesignal", "take-home test", "online assessment", "technical test")) {
            return EmailClassificationDto.builder()
                    .classification("ASSESSMENT")
                    .confidence(0.90)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        // 5. APPLICATION_CONFIRMATION
        if (containsAny(fullText, "thank you for applying", "application received", "received your application", "application confirmed", "application submitted")) {
            return EmailClassificationDto.builder()
                    .classification("APPLICATION_CONFIRMATION")
                    .confidence(0.95)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        // 6. RECRUITER_RESPONSE / INFORMATION_REQUEST
        if (containsAny(fullText, "recruiter", "talent acquisition", "follow up", "next steps", "regarding your application", "additional information")) {
            return EmailClassificationDto.builder()
                    .classification("RECRUITER_RESPONSE")
                    .confidence(0.80)
                    .extractedData(extractedData)
                    .model("HEURISTIC_RULE_ENGINE")
                    .build();
        }

        return EmailClassificationDto.builder()
                .classification("OTHER")
                .confidence(0.50)
                .extractedData(extractedData)
                .model("HEURISTIC_RULE_ENGINE")
                .build();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
