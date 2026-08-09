package com.ai.career.form.service;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.form.model.*;
import com.ai.career.form.service.impl.AnswerMappingServiceImpl;
import com.ai.career.llm.client.OllamaClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AnswerMappingServiceTest {

    private OllamaClientService ollamaClientService;
    private AnswerMappingService answerMappingService;

    @BeforeEach
    public void setUp() {
        ollamaClientService = mock(OllamaClientService.class);
        answerMappingService = new AnswerMappingServiceImpl(ollamaClientService);
    }

    @Test
    public void testDirectProfileValueMapping() {
        User user = User.builder().email("candidate@example.com").build();
        Profile profile = Profile.builder().fullName("Jane Doe").build();
        user.setProfile(profile);

        Application app = Application.builder().user(user).build();
        NormalizedFormField nameField = NormalizedFormField.builder().fieldId("name").label("Full Name").category(FieldCategory.PERSONAL_NAME).build();
        NormalizedFormField emailField = NormalizedFormField.builder().fieldId("email").label("Email").category(FieldCategory.EMAIL).build();

        FieldAnswerMapping nameMapping = answerMappingService.mapFieldAnswer(app, nameField);
        FieldAnswerMapping emailMapping = answerMappingService.mapFieldAnswer(app, emailField);

        assertEquals(MappingType.DIRECT_PROFILE_VALUE, nameMapping.getMappingType());
        assertEquals("Jane Doe", nameMapping.getProposedValue());
        assertFalse(nameMapping.isRequiresReview());

        assertEquals(MappingType.DIRECT_PROFILE_VALUE, emailMapping.getMappingType());
        assertEquals("candidate@example.com", emailMapping.getProposedValue());
    }

    @Test
    public void testStrictAntiFabricationPolicyWhenCandidateFactUnavailable() {
        User user = User.builder().email("candidate@example.com").build();
        user.setProfile(Profile.builder().fullName("Jane Doe").build()); // No work auth, no salary

        Application app = Application.builder().user(user).build();

        NormalizedFormField workAuthField = NormalizedFormField.builder().fieldId("work_auth").label("Work Authorization").category(FieldCategory.WORK_AUTHORIZATION).required(true).build();
        NormalizedFormField salaryField = NormalizedFormField.builder().fieldId("salary").label("Desired Salary").category(FieldCategory.SALARY).required(true).build();

        FieldAnswerMapping workAuthMapping = answerMappingService.mapFieldAnswer(app, workAuthField);
        FieldAnswerMapping salaryMapping = answerMappingService.mapFieldAnswer(app, salaryField);

        // Anti-Fabrication Rule: Must map to USER_REQUIRED with null value, NEVER fabricate facts
        assertEquals(MappingType.USER_REQUIRED, workAuthMapping.getMappingType());
        assertNull(workAuthMapping.getProposedValue());
        assertTrue(workAuthMapping.isRequiresReview());
        assertEquals(0.0, workAuthMapping.getConfidence());

        assertEquals(MappingType.USER_REQUIRED, salaryMapping.getMappingType());
        assertNull(salaryMapping.getProposedValue());
        assertTrue(salaryMapping.isRequiresReview());
    }
}
