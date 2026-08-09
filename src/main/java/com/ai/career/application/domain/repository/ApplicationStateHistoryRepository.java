package com.ai.career.application.domain.repository;

import com.ai.career.application.domain.entity.ApplicationStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationStateHistoryRepository extends JpaRepository<ApplicationStateHistory, Long> {
    List<ApplicationStateHistory> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
