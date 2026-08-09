package com.ai.career.form.service.impl;

import com.ai.career.execution.provider.FormDefinition;
import com.ai.career.execution.provider.FormField;
import com.ai.career.form.model.FieldType;
import com.ai.career.form.model.NormalizedFormField;
import com.ai.career.form.service.FormNormalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class FormNormalizationServiceImpl implements FormNormalizationService {

    @Override
    public List<NormalizedFormField> normalizeForm(FormDefinition formDefinition) {
        if (formDefinition == null || formDefinition.getFields() == null) {
            return Collections.emptyList();
        }

        List<NormalizedFormField> normalizedList = new ArrayList<>();
        int index = 0;
        for (FormField rawField : formDefinition.getFields()) {
            normalizedList.add(normalizeField(rawField, index++));
        }

        return normalizedList;
    }

    @Override
    public NormalizedFormField normalizeField(FormField rawField, int index) {
        if (rawField == null) {
            return NormalizedFormField.builder()
                .fieldId("field_" + index)
                .type(FieldType.UNKNOWN)
                .build();
        }

        String rawName = rawField.getName() != null ? rawField.getName().trim() : "";
        String label = rawField.getLabel() != null ? rawField.getLabel().trim() : rawName;
        String normalizedLabel = label.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        FieldType fieldType = normalizeType(rawField.getType(), normalizedLabel);
        String fieldId = !rawName.isEmpty() ? rawName : "field_" + index;

        return NormalizedFormField.builder()
            .fieldId(fieldId)
            .rawName(rawName)
            .label(label)
            .normalizedLabel(normalizedLabel)
            .type(fieldType)
            .required(rawField.isRequired())
            .options(rawField.getOptions() != null ? rawField.getOptions() : Collections.emptyList())
            .fieldSelector(rawField.getFieldSelector())
            .build();
    }

    private FieldType normalizeType(String rawType, String normalizedLabel) {
        if (rawType == null) {
            rawType = "";
        }

        String t = rawType.toUpperCase().trim();
        if (t.contains("FILE") || t.contains("UPLOAD") || normalizedLabel.contains("resume") || normalizedLabel.contains("cover letter")) {
            return FieldType.FILE;
        } else if (t.contains("EMAIL") || normalizedLabel.contains("email")) {
            return FieldType.EMAIL;
        } else if (t.contains("PHONE") || t.contains("TEL") || normalizedLabel.contains("phone") || normalizedLabel.contains("mobile")) {
            return FieldType.PHONE;
        } else if (t.contains("URL") || t.contains("LINK") || normalizedLabel.contains("url") || normalizedLabel.contains("linkedin") || normalizedLabel.contains("github")) {
            return FieldType.URL;
        } else if (t.contains("TEXTAREA") || t.contains("PARAGRAPH")) {
            return FieldType.TEXTAREA;
        } else if (t.contains("SELECT") || t.contains("DROPDOWN")) {
            return FieldType.SELECT;
        } else if (t.contains("RADIO")) {
            return FieldType.RADIO;
        } else if (t.contains("CHECKBOX")) {
            return FieldType.CHECKBOX;
        } else if (t.contains("BOOLEAN")) {
            return FieldType.BOOLEAN;
        } else if (t.contains("TEXT") || t.contains("STRING") || t.contains("INPUT")) {
            return FieldType.TEXT;
        }

        return FieldType.TEXT;
    }
}
