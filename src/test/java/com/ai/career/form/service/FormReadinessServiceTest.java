package com.ai.career.form.service;

import com.ai.career.form.model.*;
import com.ai.career.form.service.impl.FormReadinessServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormReadinessServiceTest {

    private FormReadinessService readinessService;

    @BeforeEach
    public void setUp() {
        readinessService = new FormReadinessServiceImpl();
    }

    @Test
    public void testFormReadinessIsReadyWhenAllRequiredFieldsAreMappedWithHighConfidence() {
        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("name").label("Name").required(true).build();
        NormalizedFormField f2 = NormalizedFormField.builder().fieldId("email").label("Email").required(true).build();

        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("name").mappingType(MappingType.DIRECT_PROFILE_VALUE).confidence(0.95).requiresReview(false).build();
        FieldAnswerMapping m2 = FieldAnswerMapping.builder().fieldId("email").mappingType(MappingType.DIRECT_PROFILE_VALUE).confidence(0.99).requiresReview(false).build();

        FormReadinessStatus status = readinessService.evaluateReadiness(List.of(f1, f2), List.of(m1, m2));
        assertEquals(FormReadinessStatus.READY, status);
    }

    @Test
    public void testFormReadinessIsRequiresReviewWhenAIGeneratedAnswerExists() {
        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("name").label("Name").required(true).build();
        NormalizedFormField f2 = NormalizedFormField.builder().fieldId("custom_q").label("Why Hire You?").required(true).build();

        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("name").mappingType(MappingType.DIRECT_PROFILE_VALUE).confidence(0.95).requiresReview(false).build();
        FieldAnswerMapping m2 = FieldAnswerMapping.builder().fieldId("custom_q").mappingType(MappingType.AI_GENERATED).confidence(0.80).requiresReview(true).build();

        FormReadinessStatus status = readinessService.evaluateReadiness(List.of(f1, f2), List.of(m1, m2));
        assertEquals(FormReadinessStatus.REQUIRES_REVIEW, status);
    }

    @Test
    public void testFormReadinessIsNotReadyWhenRequiredFieldIsUserRequired() {
        NormalizedFormField f1 = NormalizedFormField.builder().fieldId("name").label("Name").required(true).build();
        NormalizedFormField f2 = NormalizedFormField.builder().fieldId("work_auth").label("Work Auth").required(true).build();

        FieldAnswerMapping m1 = FieldAnswerMapping.builder().fieldId("name").mappingType(MappingType.DIRECT_PROFILE_VALUE).confidence(0.95).requiresReview(false).build();
        FieldAnswerMapping m2 = FieldAnswerMapping.builder().fieldId("work_auth").mappingType(MappingType.USER_REQUIRED).confidence(0.0).requiresReview(true).build();

        FormReadinessStatus status = readinessService.evaluateReadiness(List.of(f1, f2), List.of(m1, m2));
        assertEquals(FormReadinessStatus.NOT_READY, status);
    }
}
