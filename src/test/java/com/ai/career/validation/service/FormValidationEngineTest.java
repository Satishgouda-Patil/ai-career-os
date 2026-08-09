package com.ai.career.validation.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.execution.provider.ProviderCapabilities;
import com.ai.career.execution.registry.ApplicationExecutionProviderRegistry;
import com.ai.career.form.model.*;
import com.ai.career.validation.model.*;
import com.ai.career.validation.service.impl.FormValidationEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormValidationEngineTest {

    private ApplicationExecutionProviderRegistry providerRegistry;
    private FormValidationEngine validationEngine;

    @BeforeEach
    public void setUp() {
        providerRegistry = mock(ApplicationExecutionProviderRegistry.class);
        com.ai.career.execution.provider.ApplicationExecutionProvider mockProvider = mock(com.ai.career.execution.provider.ApplicationExecutionProvider.class);
        when(mockProvider.getCapabilities()).thenReturn(ProviderCapabilities.builder().build());
        when(providerRegistry.resolve(any())).thenReturn(mockProvider);

        validationEngine = new FormValidationEngineImpl(providerRegistry);
    }

    @Test
    public void testValidApplicationYieldsValidStatusAndReadyForExecution() {
        Application app = Application.builder().id(1L).status(ApplicationState.APPROVED).providerName("MOCK").build();

        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("email").label("Email").type(FieldType.EMAIL).required(true).build();
        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("email").mappingType(MappingType.DIRECT_PROFILE_VALUE).proposedValue("test@example.com").confidence(0.99).build();

        ApplicationFormPlan plan = ApplicationFormPlan.builder().fields(List.of(f1)).mappings(List.of(m1)).build();

        ApplicationValidationResult result = validationEngine.validateApplication(app, plan);

        assertTrue(result.isValid());
        assertEquals(ApplicationValidationStatus.VALID, result.getStatus());
        assertEquals(ExecutionReadiness.READY_FOR_EXECUTION, result.getReadiness());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    public void testInvalidEmailYieldsFormatError() {
        Application app = Application.builder().id(1L).status(ApplicationState.APPROVED).providerName("MOCK").build();

        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("email").label("Email").type(FieldType.EMAIL).required(true).build();
        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("email").mappingType(MappingType.DIRECT_PROFILE_VALUE).proposedValue("not-an-email").confidence(0.99).build();

        ApplicationFormPlan plan = ApplicationFormPlan.builder().fields(List.of(f1)).mappings(List.of(m1)).build();

        ApplicationValidationResult result = validationEngine.validateApplication(app, plan);

        assertFalse(result.isValid());
        assertEquals(ApplicationValidationStatus.REQUIRES_REVIEW, result.getStatus());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.getCode() == ValidationErrorCode.INVALID_EMAIL));
    }

    @Test
    public void testInvalidStateBlocksExecutionReadiness() {
        Application app = Application.builder().id(1L).status(ApplicationState.DISCOVERED).providerName("MOCK").build();

        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("email").label("Email").type(FieldType.EMAIL).required(true).build();
        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("email").mappingType(MappingType.DIRECT_PROFILE_VALUE).proposedValue("test@example.com").confidence(0.99).build();

        ApplicationFormPlan plan = ApplicationFormPlan.builder().fields(List.of(f1)).mappings(List.of(m1)).build();

        ApplicationValidationResult result = validationEngine.validateApplication(app, plan);

        assertFalse(result.isValid());
        assertEquals(ApplicationValidationStatus.INVALID, result.getStatus());
        assertEquals(ExecutionReadiness.BLOCKED, result.getReadiness());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.getCode() == ValidationErrorCode.APPLICATION_STATE_INVALID));
    }
}
