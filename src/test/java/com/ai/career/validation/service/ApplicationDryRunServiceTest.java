package com.ai.career.validation.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.User;
import com.ai.career.execution.provider.*;
import com.ai.career.execution.provider.mock.MockApplicationExecutionProvider;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.form.model.ApplicationFormPlan;
import com.ai.career.form.service.ApplicationFormService;
import com.ai.career.validation.model.*;
import com.ai.career.validation.repository.ApplicationDryRunRepository;
import com.ai.career.validation.service.impl.ApplicationDryRunServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApplicationDryRunServiceTest {

    private ApplicationRepository applicationRepository;
    private ApplicationFormService formService;
    private FormValidationEngine validationEngine;
    private ApplicationExecutionProviderRegistry providerRegistry;
    private ApplicationDryRunRepository dryRunRepository;
    private MockApplicationExecutionProvider mockProvider;

    private ApplicationDryRunService dryRunService;

    @BeforeEach
    public void setUp() {
        applicationRepository = mock(ApplicationRepository.class);
        formService = mock(ApplicationFormService.class);
        validationEngine = mock(FormValidationEngine.class);
        providerRegistry = mock(ApplicationExecutionProviderRegistry.class);
        dryRunRepository = mock(ApplicationDryRunRepository.class);

        mockProvider = spy(new MockApplicationExecutionProvider());
        doReturn(mockProvider).when(providerRegistry).resolve(any(Application.class));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        dryRunService = new ApplicationDryRunServiceImpl(
            applicationRepository,
            formService,
            validationEngine,
            providerRegistry,
            dryRunRepository,
            mapper
        );
    }

    @Test
    public void testZeroExternalSideEffectDryRunExecution() {
        User user = User.builder().id(10L).email("dryrun@example.com").build();
        Application app = Application.builder().id(100L).user(user).status(ApplicationState.APPROVED).providerName("MOCK").build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(formService.getFormPlan(10L, 100L)).thenReturn(ApplicationFormPlan.builder().fields(List.of()).build());
        when(validationEngine.validateApplication(any(), any())).thenReturn(
            ApplicationValidationResult.builder()
                .applicationId(100L)
                .valid(true)
                .status(ApplicationValidationStatus.VALID)
                .readiness(ExecutionReadiness.READY_FOR_EXECUTION)
                .reasons(List.of())
                .build()
        );

        DryRunReport report = dryRunService.executeDryRun(10L, 100L);

        assertNotNull(report);
        assertNotNull(report.getRunId());
        assertTrue(report.getRunId().startsWith("dryrun_"));
        assertEquals("MOCK", report.getProviderName());
        assertEquals(ApplicationValidationStatus.VALID, report.getValidationStatus());
        assertEquals(ExecutionReadiness.READY_FOR_EXECUTION, report.getReadinessStatus());

        // Zero External Side Effect Guarantee Assertions:
        // 1. Mock provider validate was called locally
        verify(mockProvider, times(1)).validate(any(Application.class), any(ExecutionContext.class));

        // 2. Real execute (which submits applications) WAS NOT INVOKED
        verify(mockProvider, never()).execute(any(), any());

        // 3. Report entity persisted locally
        verify(dryRunRepository, times(1)).save(any());
    }
}
