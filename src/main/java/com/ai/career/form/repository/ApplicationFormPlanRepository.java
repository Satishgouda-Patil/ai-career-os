package com.ai.career.form.repository;

import com.ai.career.form.entity.ApplicationFormPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationFormPlanRepository extends JpaRepository<ApplicationFormPlanEntity, Long> {
    Optional<ApplicationFormPlanEntity> findByApplicationId(Long applicationId);
}
