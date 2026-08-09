package com.ai.career.execution.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDefinition {
    private String formId;
    private String actionUrl;
    private List<FormField> fields;
    private boolean hasCaptcha;
    private boolean hasMfa;
}
