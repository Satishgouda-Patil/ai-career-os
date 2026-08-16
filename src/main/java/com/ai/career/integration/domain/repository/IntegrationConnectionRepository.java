package com.ai.career.integration.domain.repository;

import com.ai.career.integration.domain.entity.IntegrationConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection, Long> {
    List<IntegrationConnection> findByUserId(Long userId);
    Optional<IntegrationConnection> findByUserIdAndProvider(Long userId, String provider);
}
