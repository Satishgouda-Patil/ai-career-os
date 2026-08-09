package com.ai.career.application.domain.repository;

import com.ai.career.application.domain.entity.ApplicationApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationApprovalRepository extends JpaRepository<ApplicationApproval, Long> {
    List<ApplicationApproval> findByApplicationIdOrderByApprovedAtDesc(Long applicationId);
}
