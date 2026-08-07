package com.ai.career.communication.domain.repository;

import com.ai.career.communication.domain.entity.EmailDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailDraftRepository extends JpaRepository<EmailDraft, Long> {

    List<EmailDraft> findByUserIdAndJobIdOrderByVersionDesc(Long userId, Long jobId);

    Optional<EmailDraft> findFirstByUserIdAndJobIdOrderByVersionDesc(Long userId, Long jobId);

    List<EmailDraft> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT MAX(ed.version) FROM EmailDraft ed WHERE ed.user.id = :userId AND ed.job.id = :jobId")
    Integer findMaxVersionByUserIdAndJobId(Long userId, Long jobId);
}
