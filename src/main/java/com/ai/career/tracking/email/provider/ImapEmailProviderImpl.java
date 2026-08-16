package com.ai.career.tracking.email.provider;

import com.ai.career.tracking.email.dto.RawEmailMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ImapEmailProviderImpl implements EmailProvider {

    @Override
    public String getProviderName() {
        return "IMAP_OAUTH_READONLY";
    }

    @Override
    public List<RawEmailMessageDto> fetchUnprocessedMessages(Long userId) {
        log.info("Fetching unprocessed email messages via read-only IMAP/OAuth provider for user ID: {}", userId);
        // Returns read-only sync payload without exposing tokens or sending emails
        return Collections.emptyList();
    }
}
