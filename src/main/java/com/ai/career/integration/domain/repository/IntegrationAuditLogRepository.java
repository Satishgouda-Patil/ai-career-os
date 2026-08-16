package com.ai.career.integration.domain.repository;

import com.ai.career.integration.domain.entity.IntegrationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrationAuditLogRepository extends JpaRepository<IntegrationAuditLog, Long> {
    List<IntegrationAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<IntegrationAuditLog> findByUserIdAndProviderNameOrderByCreatedAtDesc(Long userId, String providerName);
}
