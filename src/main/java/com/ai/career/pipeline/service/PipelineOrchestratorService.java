package com.ai.career.pipeline.service;

import com.ai.career.pipeline.dto.PipelineStatusDto;

public interface PipelineOrchestratorService {
    PipelineStatusDto triggerEndToEndPipeline(Long userId, Long jobId);
    PipelineStatusDto getPipelineStatus(Long userId, Long applicationId);
}
