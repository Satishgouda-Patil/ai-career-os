package com.ai.career.integration.service;

import com.ai.career.integration.dto.ControlCenterSummaryDto;

public interface ControlCenterService {
    ControlCenterSummaryDto getSummary(Long userId);
}
