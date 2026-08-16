package com.ai.career.tracking.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.tracking.dto.ActivityDto;

import java.util.List;

public interface NextActionEngine {
    NextActionDecision evaluateNextAction(Application application, List<ActivityDto> timeline);
}
