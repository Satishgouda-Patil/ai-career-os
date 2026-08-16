package com.ai.career.tracking.followup.domain.repository;

import com.ai.career.tracking.followup.domain.entity.ApplicationFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationFollowUpRepository extends JpaRepository<ApplicationFollowUp, Long> {

    List<ApplicationFollowUp> findByApplicationIdOrderBySequenceNumberAsc(Long applicationId);

    List<ApplicationFollowUp> findByApplicationIdAndStatus(Long applicationId, String status);

    List<ApplicationFollowUp> findByStatusAndScheduledAtBefore(String status, LocalDateTime threshold);

    Optional<ApplicationFollowUp> findTopByApplicationIdOrderBySequenceNumberDesc(Long applicationId);
}
