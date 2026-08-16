package com.ai.career.tracking.email.domain.repository;

import com.ai.career.tracking.email.domain.entity.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessage, Long> {

    List<EmailMessage> findByUserIdOrderByReceivedAtDesc(Long userId);

    List<EmailMessage> findByApplicationIdOrderByReceivedAtDesc(Long applicationId);

    Optional<EmailMessage> findByProviderAndExternalMessageId(String provider, String externalMessageId);

    List<EmailMessage> findByUserIdAndProcessedAtIsNull(Long userId);
}
