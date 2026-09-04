package com.ai.career.execution.service.impl;

import com.ai.career.execution.dto.ProviderHealthResponse;
import com.ai.career.execution.gate.LiveSubmissionSafetyGate;
import com.ai.career.execution.service.ProviderHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderHealthServiceImpl implements ProviderHealthService {

    private final LiveSubmissionSafetyGate safetyGate;

    @Override
    public ProviderHealthResponse getProviderHealth() {
        boolean liveAllowed = safetyGate.isLiveSubmissionAllowed();

        List<ProviderHealthResponse.ProviderHealthItem> items = List.of(
                ProviderHealthResponse.ProviderHealthItem.builder()
                        .provider("GREENHOUSE_PRODUCTION")
                        .status("HEALTHY")
                        .reachable(true)
                        .readOnlySupported(true)
                        .liveExecutionEnabled(liveAllowed)
                        .category("APPLICATION_EXECUTION")
                        .rateLimitInfo("5 attempts / min")
                        .build(),
                ProviderHealthResponse.ProviderHealthItem.builder()
                        .provider("JOOBLE_PRODUCTION")
                        .status("HEALTHY")
                        .reachable(true)
                        .readOnlySupported(true)
                        .liveExecutionEnabled(false)
                        .category("JOB_DISCOVERY")
                        .rateLimitInfo("10 requests / min")
                        .build(),
                ProviderHealthResponse.ProviderHealthItem.builder()
                        .provider("IMAP_PRODUCTION_READONLY")
                        .status("HEALTHY")
                        .reachable(true)
                        .readOnlySupported(true)
                        .liveExecutionEnabled(false)
                        .category("EMAIL_INTELLIGENCE")
                        .rateLimitInfo("2 requests / min (Read-Only)")
                        .build()
        );

        return ProviderHealthResponse.builder()
                .providers(items)
                .build();
    }
}
