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
public class FormField {
    private String name;
    private String label;
    private String type; // TEXT, FILE, SELECT, CHECKBOX, RADIO
    private boolean required;
    private List<String> options;
    private String fieldSelector;
}
