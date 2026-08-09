package com.ai.career.execution.provider;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.execution.provider.mock.MockApplicationExecutionProvider;
import com.ai.career.execution.provider.mock.MockScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MockApplicationExecutionProviderTest {

    private MockApplicationExecutionProvider provider;
    private Application application;

    @BeforeEach
    public void setUp() {
        provider = new MockApplicationExecutionProvider();
        application = Application.builder()
            .id(100L)
            .providerName("MOCK")
            .applicationUrl("https://example.com/jobs/100")
            .build();
    }

    @Test
    public void testCapabilitiesAndSupports() {
        assertTrue(provider.supports(application));
        assertEquals("MOCK", provider.getProviderName());

        ProviderCapabilities capabilities = provider.getCapabilities();
        assertTrue(capabilities.isSupportsDryRun());
        assertTrue(capabilities.hasCapability(ProviderCapability.FORM_APPLICATION));
        assertTrue(capabilities.hasCapability(ProviderCapability.RESUME_UPLOAD));
    }

    @Test
    public void testSuccessScenario() {
        ExecutionContext context = ExecutionContext.builder()
            .applicationId(100L)
            .build();

        ExecutionResult result = provider.execute(application, context);
        assertEquals(ExecutionOutcomeStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccessful());
        assertNotNull(result.getExternalApplicationId());
        assertFalse(result.isRetryable());
    }

    @Test
    public void testValidationFailureScenario() {
        ExecutionContext context = ExecutionContext.builder()
            .applicationId(100L)
            .applicationAnswers(Map.of("mockScenario", "VALIDATION_FAILURE"))
            .build();

        ExecutionValidationResult validation = provider.validate(application, context);
        assertFalse(validation.isValid());
        assertFalse(validation.getMissingFields().isEmpty());

        ExecutionResult result = provider.execute(application, context);
        assertEquals(ExecutionOutcomeStatus.FAILED, result.getStatus());
        assertEquals("VALIDATION_ERROR", result.getErrorCode());
    }

    @Test
    public void testRequiresHumanScenario() {
        ExecutionContext context = ExecutionContext.builder()
            .applicationId(100L)
            .applicationAnswers(Map.of("mockScenario", "REQUIRES_HUMAN"))
            .build();

        ExecutionResult result = provider.execute(application, context);
        assertEquals(ExecutionOutcomeStatus.REQUIRES_HUMAN, result.getStatus());
        assertEquals("CAPTCHA_ENCOUNTERED", result.getErrorCode());
    }

    @Test
    public void testRetryableFailureScenario() {
        ExecutionContext context = ExecutionContext.builder()
            .applicationId(100L)
            .applicationAnswers(Map.of("mockScenario", "RETRYABLE_FAILURE"))
            .build();

        ExecutionResult result = provider.execute(application, context);
        assertEquals(ExecutionOutcomeStatus.FAILED, result.getStatus());
        assertTrue(result.isRetryable());
    }

    @Test
    public void testUnknownScenarioSafety() {
        ExecutionContext context = ExecutionContext.builder()
            .applicationId(100L)
            .applicationAnswers(Map.of("mockScenario", "UNKNOWN"))
            .build();

        ExecutionResult result = provider.execute(application, context);
        assertEquals(ExecutionOutcomeStatus.UNKNOWN, result.getStatus());
        assertFalse(result.isSuccessful());
    }
}
