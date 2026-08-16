package com.ai.career.browser.discovery;

import com.ai.career.form.model.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredField {
    private String id;
    private String name;
    private String tag;
    private String type;
    private FieldType fieldType;
    private String label;
    private String labelSource;
    private String placeholder;
    private boolean required;
    private String requiredSource;
    private boolean disabled;
    private boolean readonly;
    private String selector;
    private String ariaLabel;
    private String description;

    @Builder.Default
    private List<DiscoveredOption> options = new ArrayList<>();
}
