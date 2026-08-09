package com.ai.career.form.service.impl;

import com.ai.career.form.model.FieldCategory;
import com.ai.career.form.model.FieldType;
import com.ai.career.form.model.NormalizedFormField;
import com.ai.career.form.service.FieldClassificationService;
import com.ai.career.llm.client.OllamaClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldClassificationServiceImpl implements FieldClassificationService {

    private final OllamaClientService ollamaClientService;
    private final ObjectMapper objectMapper;

    @Override
    public FieldCategory classifyField(NormalizedFormField field) {
        if (field == null) {
            return FieldCategory.OTHER;
        }

        // 1. Rule-Based Classification
        FieldCategory category = classifyByRules(field);
        if (category != FieldCategory.OTHER) {
            log.debug("Rule-based classification matched category [{}] for field [{}]", category, field.getLabel());
            return category;
        }

        // 2. LLM Fallback via AIOrchestrator for ambiguous or custom fields
        return classifyByAI(field);
    }

    @Override
    public double getClassificationConfidence(NormalizedFormField field, FieldCategory category) {
        if (category == FieldCategory.OTHER) {
            return 0.50;
        }
        if (category == FieldCategory.CUSTOM_QUESTION) {
            return 0.80;
        }
        return 0.95;
    }

    private FieldCategory classifyByRules(NormalizedFormField field) {
        String label = field.getNormalizedLabel();
        String name = field.getRawName().toLowerCase();

        if (label.contains("first name") || label.contains("last name") || label.contains("full name") || name.contains("name")) {
            return FieldCategory.PERSONAL_NAME;
        }
        if (label.contains("email") || name.contains("email")) {
            return FieldCategory.EMAIL;
        }
        if (label.contains("phone") || label.contains("mobile") || name.contains("phone")) {
            return FieldCategory.PHONE;
        }
        if (label.contains("linkedin")) {
            return FieldCategory.LINKEDIN;
        }
        if (label.contains("github")) {
            return FieldCategory.GITHUB;
        }
        if (label.contains("portfolio") || label.contains("website") || label.contains("personal site")) {
            return FieldCategory.PORTFOLIO;
        }
        if (label.contains("resume") || label.contains("cv") || name.contains("resume")) {
            return FieldCategory.RESUME;
        }
        if (label.contains("cover letter") || name.contains("cover_letter")) {
            return FieldCategory.COVER_LETTER;
        }
        if (label.contains("work authorization") || label.contains("authorized to work") || label.contains("legally authorized")) {
            return FieldCategory.WORK_AUTHORIZATION;
        }
        if (label.contains("visa") || label.contains("sponsorship") || label.contains("require sponsorship")) {
            return FieldCategory.VISA;
        }
        if (label.contains("years of experience") || label.contains("experience in years")) {
            return FieldCategory.YEARS_OF_EXPERIENCE;
        }
        if (label.contains("salary") || label.contains("compensation") || label.contains("desired pay")) {
            return FieldCategory.SALARY;
        }
        if (label.contains("notice period") || label.contains("how soon can you start")) {
            return FieldCategory.NOTICE_PERIOD;
        }
        if (label.contains("relocat")) {
            return FieldCategory.RELOCATION;
        }
        if (label.contains("work mode") || label.contains("remote preference")) {
            return FieldCategory.WORK_MODE;
        }
        if (field.getType() == FieldType.TEXTAREA || label.contains("why do you want") || label.contains("describe your") || label.contains("tell us about")) {
            return FieldCategory.CUSTOM_QUESTION;
        }

        return FieldCategory.OTHER;
    }

    private FieldCategory classifyByAI(NormalizedFormField field) {
        log.info("Invoking AIOrchestrator fallback classification for field: [{}]", field.getLabel());
        try {
            String prompt = String.format(
                "Classify the following form field into exactly one category.\n" +
                "Field Label: \"%s\"\nField Type: \"%s\"\n" +
                "Allowed Categories: [PERSONAL_NAME, EMAIL, PHONE, LOCATION, LINKEDIN, GITHUB, PORTFOLIO, RESUME, COVER_LETTER, WORK_AUTHORIZATION, VISA, YEARS_OF_EXPERIENCE, EDUCATION, SKILL, SALARY, NOTICE_PERIOD, RELOCATION, WORK_MODE, CUSTOM_QUESTION, OTHER]\n\n" +
                "Respond ONLY with valid JSON in this exact structure: {\"category\": \"CATEGORY_NAME\", \"confidence\": 0.85}",
                field.getLabel(), field.getType()
            );

            String aiResponse = ollamaClientService.generateCompletion(prompt);
            JsonNode root = objectMapper.readTree(aiResponse);
            if (root.has("category")) {
                String catStr = root.get("category").asText();
                return FieldCategory.valueOf(catStr.toUpperCase());
            }
        } catch (Exception ex) {
            log.warn("AI classification fallback failed or returned invalid JSON for field [{}]: {}. Falling back to OTHER.", field.getLabel(), ex.getMessage());
        }

        return FieldCategory.OTHER;
    }
}
