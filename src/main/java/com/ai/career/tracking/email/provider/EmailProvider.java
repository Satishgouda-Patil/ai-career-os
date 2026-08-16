package com.ai.career.tracking.email.provider;

import com.ai.career.tracking.email.dto.RawEmailMessageDto;

import java.util.List;

public interface EmailProvider {
    String getProviderName();
    List<RawEmailMessageDto> fetchUnprocessedMessages(Long userId);
}
