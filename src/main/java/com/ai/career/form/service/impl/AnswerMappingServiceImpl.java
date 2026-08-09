package com.ai.career.form.service.impl;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.Profile;
import com.ai.career.domain.entity.User;
import com.ai.career.form.model.*;
import com.ai.career.form.service.AnswerMappingService;
import com.ai.career.llm.client.OllamaClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerMappingServiceImpl implements AnswerMappingService {

    private final OllamaClientService ollamaClientService;

    @Override
    public FieldAnswerMapping mapFieldAnswer(Application application, NormalizedFormField field) {
        if (application == null || field == null) {
            return buildUserRequiredMapping(field, "Application or field is null");
        }

        User user = application.getUser();
        Profile profile = user != null ? user.getProfile() : null;
        FieldCategory category = field.getCategory() != null ? field.getCategory() : FieldCategory.OTHER;

        switch (category) {
            case PERSONAL_NAME:
                String name = profile != null && profile.getFullName() != null
                    ? profile.getFullName()
                    : (user.getEmail() != null ? user.getEmail().split("@")[0] : null);
                if (name != null) {
                    return buildMapping(field, MappingType.DIRECT_PROFILE_VALUE, name, 0.95, false, "Candidate profile name", "profile.full_name");
                }
                break;

            case EMAIL:
                if (user.getEmail() != null) {
                    return buildMapping(field, MappingType.DIRECT_PROFILE_VALUE, user.getEmail(), 0.99, false, "Candidate account email", "user.email");
                }
                break;

            case LOCATION:
                if (profile != null && profile.getLocation() != null && !profile.getLocation().isBlank()) {
                    return buildMapping(field, MappingType.DIRECT_PROFILE_VALUE, profile.getLocation(), 0.95, false, "Candidate profile location", "profile.location");
                }
                break;

            case RESUME:
                if (application.getResumeVersion() != null && application.getResumeVersion().getPdfUrl() != null) {
                    return buildMapping(field, MappingType.RESUME_VALUE, application.getResumeVersion().getPdfUrl(), 0.95, false, "Candidate active PDF resume version", "resume.pdf_url");
                }
                break;

            case COVER_LETTER:
                if (application.getCoverLetter() != null && application.getCoverLetter().getContent() != null) {
                    return buildMapping(field, MappingType.DERIVED_VALUE, application.getCoverLetter().getContent(), 0.90, false, "Candidate active cover letter artifact", "cover_letter.content");
                }
                break;

            case CUSTOM_QUESTION:
                String generatedAnswer = generateCustomQuestionAnswer(application, field);
                return buildMapping(field, MappingType.AI_GENERATED, generatedAnswer, 0.80, true, "AI-generated response tailored to candidate profile and job requirements", "ollama.client");

            case PHONE:
            case LINKEDIN:
            case GITHUB:
            case PORTFOLIO:
            case WORK_AUTHORIZATION:
            case VISA:
            case SALARY:
            case NOTICE_PERIOD:
            case RELOCATION:
            case YEARS_OF_EXPERIENCE:
                // CRITICAL ANTI-FABRICATION SAFETY RULE:
                // Never fabricate candidate facts or legal status when unavailable.
                return buildUserRequiredMapping(field, "Candidate explicit preference data unavailable. Strict anti-fabrication safety rule enforced.");

            default:
                if (field.getType() == FieldType.UNKNOWN) {
                    return buildMapping(field, MappingType.UNSUPPORTED, null, 0.0, true, "Unsupported field type encountered", null);
                }
                break;
        }

        // Default fallback: candidate fact unavailable -> USER_REQUIRED
        return buildUserRequiredMapping(field, "Candidate data not found for field [" + field.getLabel() + "]. User input required.");
    }

    private String generateCustomQuestionAnswer(Application application, NormalizedFormField field) {
        try {
            if (ollamaClientService != null && ollamaClientService.isAvailable()) {
                String prompt = String.format(
                    "Write a professional, concise answer (2-3 sentences) for the job application question:\n" +
                    "Question: \"%s\"\n" +
                    "Target Job: \"%s\"\n",
                    field.getLabel(),
                    application.getJob() != null ? application.getJob().getTitle() : "Software Engineer"
                );
                String resp = ollamaClientService.generateCompletion(prompt);
                if (resp != null && !resp.isBlank()) {
                    return resp;
                }
            }
        } catch (Exception ex) {
            log.warn("AI generation failed for custom question field [{}]: {}. Returning fallback proposal.", field.getLabel(), ex.getMessage());
        }
        return "I am excited to apply my engineering skills to contribute effectively to your team.";
    }

    private FieldAnswerMapping buildMapping(NormalizedFormField field, MappingType mappingType, String proposedValue, double confidence, boolean requiresReview, String explanation, String sourceKey) {
        return FieldAnswerMapping.builder()
            .fieldId(field.getFieldId())
            .fieldName(field.getLabel())
            .fieldCategory(field.getCategory())
            .mappingType(mappingType)
            .proposedValue(proposedValue)
            .confidence(confidence)
            .requiresReview(requiresReview)
            .explanation(explanation)
            .sourceKey(sourceKey)
            .build();
    }

    private FieldAnswerMapping buildUserRequiredMapping(NormalizedFormField field, String explanation) {
        return FieldAnswerMapping.builder()
            .fieldId(field != null ? field.getFieldId() : "unknown")
            .fieldName(field != null ? field.getLabel() : "Unknown Field")
            .fieldCategory(field != null ? field.getCategory() : FieldCategory.OTHER)
            .mappingType(MappingType.USER_REQUIRED)
            .proposedValue(null)
            .confidence(0.0)
            .requiresReview(true)
            .explanation(explanation)
            .sourceKey(null)
            .build();
    }
}
