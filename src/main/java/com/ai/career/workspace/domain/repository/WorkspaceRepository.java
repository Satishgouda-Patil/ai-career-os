package com.ai.career.workspace.domain.repository;

import com.ai.career.workspace.domain.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByUserIdAndJobId(Long userId, Long jobId);
}
