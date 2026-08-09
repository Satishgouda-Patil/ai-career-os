package com.ai.career.form.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldAnswerMapping {
    private String fieldId;
    private String fieldName;
    private FieldCategory fieldCategory;
    private MappingType mappingType;
    private String proposedValue;
    private double confidence; // 0.0 to 1.0
    private boolean requiresReview;
    private String explanation;
    private String sourceKey;
}
