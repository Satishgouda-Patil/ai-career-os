package com.ai.career.tracking.email;

import com.ai.career.application.domain.entity.Application;

import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.tracking.email.service.ApplicationMatcherService;
import com.ai.career.tracking.email.service.impl.ApplicationMatcherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationMatcherTest {

    @Mock
    private ApplicationRepository applicationRepository;

    private ApplicationMatcherService applicationMatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationMatcher = new ApplicationMatcherServiceImpl(applicationRepository);
    }

    @Test
    void testMatchByCompanyAndJobTitle() {
        Job job = Job.builder().company("Acme AI").title("Senior Java Architect").build();
        Application app = Application.builder().id(100L).job(job).build();

        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(app));

        Optional<ApplicationMatcherService.ApplicationMatchResult> match = applicationMatcher.matchApplication(
                1L,
                "recruiter@acme.com",
                "Interview for Senior Java Architect at Acme AI",
                "Hi, let's schedule an interview.",
                "thread-123"
        );

        assertTrue(match.isPresent());
        assertEquals(100L, match.get().application().getId());
        assertTrue(match.get().matchConfidence() >= 0.85);
    }

    @Test
    void testExplicitApplicationIdInText() {
        Job job = Job.builder().company("TechCorp").title("Software Developer").build();
        Application app = Application.builder().id(456L).job(job).build();

        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(app));

        Optional<ApplicationMatcherService.ApplicationMatchResult> match = applicationMatcher.matchApplication(
                1L,
                "hr@techcorp.com",
                "Update regarding Application ID: 456",
                "Thank you for your submission.",
                "thread-456"
        );

        assertTrue(match.isPresent());
        assertEquals(456L, match.get().application().getId());
        assertEquals(0.99, match.get().matchConfidence());
    }

    @Test
    void testLowConfidenceNoMatch() {
        Job job = Job.builder().company("Google").title("Staff Engineer").build();
        Application app = Application.builder().id(100L).job(job).build();

        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(app));

        Optional<ApplicationMatcherService.ApplicationMatchResult> match = applicationMatcher.matchApplication(
                1L,
                "spam@randomsite.com",
                "Discount on software licenses",
                "Buy our software today!",
                "thread-999"
        );

        assertTrue(match.isEmpty());
    }
}
