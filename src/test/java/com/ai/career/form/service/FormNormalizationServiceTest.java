package com.ai.career.form.service;

import com.ai.career.execution.provider.FormDefinition;
import com.ai.career.form.fixture.MockFormFixtures;
import com.ai.career.form.model.FieldType;
import com.ai.career.form.model.NormalizedFormField;
import com.ai.career.form.service.impl.FormNormalizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FormNormalizationServiceTest {

    private FormNormalizationService normalizationService;

    @BeforeEach
    public void setUp() {
        normalizationService = new FormNormalizationServiceImpl();
    }

    @Test
    public void testFormNormalizationTrimsAndNormalizesFields() {
        FormDefinition rawForm = MockFormFixtures.createSimpleForm();
        List<NormalizedFormField> normalized = normalizationService.normalizeForm(rawForm);

        assertNotNull(normalized);
        assertEquals(4, normalized.size());

        NormalizedFormField f1 = normalized.get(0);
        assertEquals("first_name", f1.getFieldId());
        assertEquals("first name", f1.getNormalizedLabel());
        assertEquals(FieldType.TEXT, f1.getType());
        assertTrue(f1.isRequired());

        NormalizedFormField f2 = normalized.get(1);
        assertEquals("email", f2.getFieldId());
        assertEquals("email", f2.getNormalizedLabel());
        assertEquals(FieldType.EMAIL, f2.getType());

        NormalizedFormField f3 = normalized.get(2);
        assertEquals("linkedin", f3.getFieldId());
        assertEquals("linkedin profile", f3.getNormalizedLabel());
        assertEquals(FieldType.URL, f3.getType());
        assertFalse(f3.isRequired());

        NormalizedFormField f4 = normalized.get(3);
        assertEquals("resume", f4.getFieldId());
        assertEquals(FieldType.FILE, f4.getType());
    }
}
