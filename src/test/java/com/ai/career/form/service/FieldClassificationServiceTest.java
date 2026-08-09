package com.ai.career.form.service;

import com.ai.career.form.model.FieldCategory;
import com.ai.career.form.model.FieldType;
import com.ai.career.form.model.NormalizedFormField;
import com.ai.career.form.service.impl.FieldClassificationServiceImpl;
import com.ai.career.llm.client.OllamaClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldClassificationServiceTest {

    private OllamaClientService ollamaClientService;
    private FieldClassificationService classificationService;

    @BeforeEach
    public void setUp() {
        ollamaClientService = mock(OllamaClientService.class);
        classificationService = new FieldClassificationServiceImpl(ollamaClientService, new ObjectMapper());
    }

    @Test
    public void testRuleBasedClassificationForStandardFields() {
        NormalizedFormField nameField = NormalizedFormField.builder().rawName("first_name").label("First Name").normalizedLabel("first name").type(FieldType.TEXT).build();
        NormalizedFormField emailField = NormalizedFormField.builder().rawName("email").label("Email Address").normalizedLabel("email address").type(FieldType.EMAIL).build();
        NormalizedFormField linkedinField = NormalizedFormField.builder().rawName("linkedin").label("LinkedIn URL").normalizedLabel("linkedin url").type(FieldType.URL).build();
        NormalizedFormField workAuthField = NormalizedFormField.builder().rawName("work_auth").label("Are you legally authorized to work in US?").normalizedLabel("are you legally authorized to work in us").type(FieldType.SELECT).build();

        assertEquals(FieldCategory.PERSONAL_NAME, classificationService.classifyField(nameField));
        assertEquals(FieldCategory.EMAIL, classificationService.classifyField(emailField));
        assertEquals(FieldCategory.LINKEDIN, classificationService.classifyField(linkedinField));
        assertEquals(FieldCategory.WORK_AUTHORIZATION, classificationService.classifyField(workAuthField));
    }

    @Test
    public void testAIFallbackClassificationWhenRulesReturnOther() {
        NormalizedFormField ambiguousField = NormalizedFormField.builder().rawName("ambiguous_q").label("Special role alignment factor").normalizedLabel("special role alignment factor").type(FieldType.TEXT).build();

        when(ollamaClientService.generateCompletion(anyString()))
            .thenReturn("{\"category\": \"CUSTOM_QUESTION\", \"confidence\": 0.85}");

        FieldCategory category = classificationService.classifyField(ambiguousField);
        assertEquals(FieldCategory.CUSTOM_QUESTION, category);
    }

    @Test
    public void testAIFallbackHandlesOllamaUnavailableGracefully() {
        NormalizedFormField ambiguousField = NormalizedFormField.builder().rawName("ambiguous_q").label("Unknown input factor").normalizedLabel("unknown input factor").type(FieldType.TEXT).build();

        when(ollamaClientService.generateCompletion(anyString()))
            .thenThrow(new RuntimeException("Ollama unavailable"));

        FieldCategory category = classificationService.classifyField(ambiguousField);
        assertEquals(FieldCategory.OTHER, category);
    }
}
