package com.ai.career.tracking.email.provider;

import com.ai.career.tracking.email.dto.RawEmailMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SimulatedEmailProvider implements EmailProvider {

    private final ConcurrentHashMap<Long, List<RawEmailMessageDto>> pendingMessages = new ConcurrentHashMap<>();

    @Override
    public String getProviderName() {
        return "SIMULATED";
    }

    public void addSimulatedMessage(Long userId, RawEmailMessageDto rawMessage) {
        pendingMessages.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(rawMessage);
        log.info("Simulated email queued for User ID {}: Subject: '{}'", userId, rawMessage.getSubject());
    }

    @Override
    public List<RawEmailMessageDto> fetchUnprocessedMessages(Long userId) {
        List<RawEmailMessageDto> messages = pendingMessages.remove(userId);
        if (messages == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(messages);
    }
}
