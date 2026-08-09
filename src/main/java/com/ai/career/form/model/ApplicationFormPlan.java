package com.ai.career.form.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFormPlan {
    private Long applicationId;
    private String providerName;
    private String formId;
    private List<NormalizedFormField> fields;
    private List<FieldAnswerMapping> mappings;
    private List<String> unresolvedFields;
    private List<String> reviewRequiredFields;
    private FormReadinessStatus readinessStatus;
    private LocalDateTime generatedAt;
}
