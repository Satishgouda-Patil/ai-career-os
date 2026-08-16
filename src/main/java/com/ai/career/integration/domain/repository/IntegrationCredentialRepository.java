package com.ai.career.integration.domain.repository;

import com.ai.career.integration.domain.entity.IntegrationCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationCredentialRepository extends JpaRepository<IntegrationCredential, Long> {
    List<IntegrationCredential> findByUserId(Long userId);
    Optional<IntegrationCredential> findByUserIdAndProviderName(Long userId, String providerName);
}
