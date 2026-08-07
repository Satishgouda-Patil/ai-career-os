package com.ai.career.workspace.service;

import com.ai.career.workspace.dto.WorkspaceResponse;

public interface WorkspaceService {
    WorkspaceResponse buildWorkspace(Long userId, Long jobId);
    WorkspaceResponse getWorkspace(Long userId, Long jobId);
    WorkspaceResponse approveWorkspace(Long userId, Long jobId);
    WorkspaceResponse rejectWorkspace(Long userId, Long jobId);
    WorkspaceResponse regenerateWorkspace(Long userId, Long jobId);
}
