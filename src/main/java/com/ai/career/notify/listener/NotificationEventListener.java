package com.ai.career.notify.listener;

import com.ai.career.match.event.MatchFoundEvent;
import com.ai.career.notify.service.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final TelegramNotificationService telegramNotificationService;

    @RabbitListener(queues = "${app.rabbitmq.queue.notify:notification.queue}")
    public void handleMatchFoundEvent(MatchFoundEvent event) {
        log.info("Received MatchFoundEvent for User ID {} and Job ID {} with score {}", event.getUserId(), event.getJobId(), event.getScore());
        telegramNotificationService.sendMatchNotification(event);
    }
}
