package com.ai.career.tracking.email.domain.repository;

import com.ai.career.tracking.email.domain.entity.EmailClassificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailClassificationResultRepository extends JpaRepository<EmailClassificationResult, Long> {

    List<EmailClassificationResult> findByEmailMessageId(Long emailMessageId);

    Optional<EmailClassificationResult> findTopByEmailMessageIdOrderByCreatedAtDesc(Long emailMessageId);
}
