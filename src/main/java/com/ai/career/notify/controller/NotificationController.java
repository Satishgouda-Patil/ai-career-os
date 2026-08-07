package com.ai.career.notify.controller;

import com.ai.career.domain.entity.Notification;
import com.ai.career.domain.repository.NotificationRepository;
import com.ai.career.notify.service.TelegramNotificationService;
import com.ai.career.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for viewing notification audit logs and triggering test Telegram alerts")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final TelegramNotificationService telegramNotificationService;

    @GetMapping
    @Operation(summary = "Get user notification audit history")
    public ResponseEntity<List<Notification>> getUserNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderBySentAtDesc(userPrincipal.getId());
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/test")
    @Operation(summary = "Send a test Telegram alert message")
    public ResponseEntity<Map<String, Object>> sendTestNotification(@RequestParam(value = "message", defaultValue = "Test alert from AI Career OS") String message) {
        boolean sent = telegramNotificationService.sendTelegramMessage("📢 *Test Notification:* " + message);
        return ResponseEntity.ok(Map.of(
            "message", message,
            "delivered", sent
        ));
    }
}
