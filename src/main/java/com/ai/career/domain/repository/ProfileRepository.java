package com.ai.career.domain.repository;

import com.ai.career.domain.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.skills WHERE p.userId = :userId")
    Optional<Profile> findByUserIdWithSkills(@Param("userId") Long userId);
}
