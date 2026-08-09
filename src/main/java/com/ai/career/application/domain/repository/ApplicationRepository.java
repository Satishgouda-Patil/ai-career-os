package com.ai.career.application.domain.repository;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserId(Long userId);

    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);

    List<Application> findByUserIdAndStatus(Long userId, ApplicationState status);

    @Query("SELECT a FROM Application a WHERE a.user.id = :userId AND a.job.id = :jobId AND a.status IN :activeStates")
    Optional<Application> findActiveApplicationByUserIdAndJobId(Long userId, Long jobId, List<ApplicationState> activeStates);
}
