package com.ai.career.notify.service;

import com.ai.career.domain.entity.JobMatch;
import com.ai.career.domain.entity.Notification;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobMatchRepository;
import com.ai.career.domain.repository.NotificationRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.match.event.MatchFoundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JobMatchRepository jobMatchRepository;

    @Value("${app.telegram.bot-token:mock-bot-token}")
    private String botToken;

    @Value("${app.telegram.chat-id:mock-chat-id}")
    private String chatId;

    @Transactional
    public void sendMatchNotification(MatchFoundEvent event) {
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null) return;

        JobMatch jobMatch = jobMatchRepository.findById(event.getMatchId()).orElse(null);

        String messageText = String.format(
            "🚀 *High Job Match Alert!*\n\n" +
            "📌 *Title:* %s\n" +
            "🏢 *Company:* %s\n" +
            "🎯 *Match Score:* %d%%\n\n" +
            "Check your AI Career OS dashboard for full details!",
            event.getJobTitle(), event.getCompany(), event.getScore()
        );

        boolean delivered = sendTelegramMessage(messageText);

        Notification notification = Notification.builder()
            .user(user)
            .jobMatch(jobMatch)
            .type("TELEGRAM_MATCH_ALERT")
            .message(messageText)
            .delivered(delivered)
            .build();

        notificationRepository.save(notification);
        log.info("Saved notification log for User ID {}. Delivered: {}", event.getUserId(), delivered);
    }

    public boolean sendTelegramMessage(String messageText) {
        if ("mock-bot-token".equalsIgnoreCase(botToken)) {
            log.info("[MOCK TELEGRAM SERVICE] Message: {}", messageText.replace("\n", " "));
            return true;
        }

        try {
            RestClient restClient = RestClient.create("https://api.telegram.org");
            restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .body(java.util.Map.of(
                    "chat_id", chatId,
                    "text", messageText,
                    "parse_mode", "Markdown"
                ))
                .retrieve()
                .toBodilessEntity();

            return true;
        } catch (Exception ex) {
            log.error("Failed to send Telegram message: {}", ex.getMessage());
            return false;
        }
    }
}
