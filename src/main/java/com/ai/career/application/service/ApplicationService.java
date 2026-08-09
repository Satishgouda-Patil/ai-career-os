package com.ai.career.application.service;

import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.dto.ApplicationHistoryResponse;
import com.ai.career.application.dto.ApplicationResponse;
import com.ai.career.application.dto.CreateApplicationRequest;
import com.ai.career.application.dto.TransitionStateRequest;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse createApplication(Long userId, CreateApplicationRequest request);
    ApplicationResponse transitionState(Long userId, Long applicationId, TransitionStateRequest request);
    ApplicationResponse getApplicationById(Long userId, Long applicationId);
    List<ApplicationResponse> getUserApplications(Long userId, ApplicationState status);
    List<ApplicationHistoryResponse> getApplicationHistory(Long userId, Long applicationId);
}
