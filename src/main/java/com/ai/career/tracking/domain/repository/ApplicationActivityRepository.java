package com.ai.career.tracking.domain.repository;

import com.ai.career.tracking.domain.entity.ApplicationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationActivityRepository extends JpaRepository<ApplicationActivity, Long> {

    List<ApplicationActivity> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    List<ApplicationActivity> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);

    Optional<ApplicationActivity> findTopByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
