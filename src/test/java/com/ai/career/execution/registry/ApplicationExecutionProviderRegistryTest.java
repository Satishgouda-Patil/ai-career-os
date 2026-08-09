package com.ai.career.execution.registry;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.execution.provider.ApplicationExecutionProvider;
import com.ai.career.execution.provider.mock.MockApplicationExecutionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationExecutionProviderRegistryTest {

    private ApplicationExecutionProviderRegistry registry;
    private MockApplicationExecutionProvider mockProvider;

    @BeforeEach
    public void setUp() {
        mockProvider = new MockApplicationExecutionProvider();
        registry = new ApplicationExecutionProviderRegistry(List.of(mockProvider));
    }

    @Test
    public void testResolveMatchingProvider() {
        Application app = Application.builder().id(1L).providerName("MOCK").build();
        ApplicationExecutionProvider resolved = registry.resolve(app);

        assertNotNull(resolved);
        assertEquals("MOCK", resolved.getProviderName());
    }

    @Test
    public void testResolveUnsupportedProviderThrowsException() {
        ApplicationExecutionProviderRegistry emptyRegistry = new ApplicationExecutionProviderRegistry(Collections.emptyList());
        Application app = Application.builder().id(2L).providerName("UNSUPPORTED").build();

        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> emptyRegistry.resolve(app)
        );
        assertTrue(ex.getMessage().contains("No execution provider available"));
    }
}
