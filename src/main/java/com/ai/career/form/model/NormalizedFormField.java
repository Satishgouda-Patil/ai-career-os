package com.ai.career.form.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedFormField {
    private String fieldId;
    private String rawName;
    private String label;
    private String normalizedLabel;
    private FieldType type;
    private boolean required;
    private List<String> options;
    private String placeholder;
    private String helpText;
    private FieldCategory category;
    private String fieldSelector;
}
