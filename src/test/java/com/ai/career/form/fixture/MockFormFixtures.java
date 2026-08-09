package com.ai.career.form.fixture;

import com.ai.career.execution.provider.FormDefinition;
import com.ai.career.execution.provider.FormField;

import java.util.List;

public class MockFormFixtures {

    public static FormDefinition createSimpleForm() {
        return FormDefinition.builder()
            .formId("form_simple")
            .actionUrl("https://example.com/apply")
            .fields(List.of(
                FormField.builder().name("first_name").label("First Name").type("TEXT").required(true).build(),
                FormField.builder().name("email").label("Email").type("EMAIL").required(true).build(),
                FormField.builder().name("linkedin").label("LinkedIn Profile").type("URL").required(false).build(),
                FormField.builder().name("resume").label("Resume File").type("FILE").required(true).build()
            ))
            .build();
    }

    public static FormDefinition createTechnicalFormWithCustomQuestions() {
        return FormDefinition.builder()
            .formId("form_technical")
            .actionUrl("https://example.com/tech_apply")
            .fields(List.of(
                FormField.builder().name("applicant_name").label("Full Name").type("TEXT").required(true).build(),
                FormField.builder().name("email_addr").label("Email Address").type("EMAIL").required(true).build(),
                FormField.builder().name("github").label("GitHub URL").type("URL").required(false).build(),
                FormField.builder().name("custom_q1").label("Describe your experience with distributed systems.").type("TEXTAREA").required(true).build()
            ))
            .build();
    }

    public static FormDefinition createFormWithLegalAuthorization() {
        return FormDefinition.builder()
            .formId("form_legal")
            .actionUrl("https://example.com/legal_apply")
            .fields(List.of(
                FormField.builder().name("email").label("Email").type("EMAIL").required(true).build(),
                FormField.builder().name("work_auth").label("Are you legally authorized to work in the United States?").type("SELECT").options(List.of("Yes", "No")).required(true).build(),
                FormField.builder().name("visa_sponsorship").label("Will you now or in the future require visa sponsorship?").type("RADIO").options(List.of("Yes", "No")).required(true).build()
            ))
            .build();
    }

    public static FormDefinition createFormWithUnsupportedAndAmbiguousFields() {
        return FormDefinition.builder()
            .formId("form_ambiguous")
            .actionUrl("https://example.com/ambiguous_apply")
            .fields(List.of(
                FormField.builder().name("custom_matrix").label("Multi-dimensional matrix input").type("UNKNOWN").required(true).build()
            ))
            .build();
    }
}
