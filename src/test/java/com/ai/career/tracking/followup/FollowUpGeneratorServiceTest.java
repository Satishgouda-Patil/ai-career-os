package com.ai.career.tracking.followup;

import com.ai.career.application.domain.entity.Application;

import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.tracking.followup.domain.entity.ApplicationFollowUp;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
import com.ai.career.tracking.followup.dto.FollowUpDto;
import com.ai.career.tracking.followup.service.FollowUpGeneratorService;
import com.ai.career.tracking.followup.service.impl.FollowUpGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpGeneratorServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationFollowUpRepository followUpRepository;

    private FollowUpGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generatorService = new FollowUpGeneratorServiceImpl(applicationRepository, followUpRepository);
    }

    @Test
    void testGenerateFirstFollowUpDraft() {
        User user = User.builder().id(1L).email("jane.doe@example.com").build();
        Job job = Job.builder().company("Google").title("Staff Software Engineer").build();
        Application app = Application.builder().id(20L).user(user).job(job).build();

        when(applicationRepository.findById(20L)).thenReturn(Optional.of(app));
        when(followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(20L)).thenReturn(Collections.emptyList());

        when(followUpRepository.save(any(ApplicationFollowUp.class))).thenAnswer(invocation -> {
            ApplicationFollowUp entity = invocation.getArgument(0);
            entity.setId(200L);
            return entity;
        });

        FollowUpDto draft = generatorService.generateFollowUpDraft(1L, 20L, 1, "Mentioning recent blog post.");

        assertNotNull(draft);
        assertEquals(1, draft.getSequenceNumber());
        assertEquals("READY", draft.getStatus());
        assertTrue(draft.getFollowUpSubject().contains("Following Up: Application for Staff Software Engineer at Google"));
        assertTrue(draft.getFollowUpBody().contains("Google"));
        assertTrue(draft.getFollowUpBody().contains("Mentioning recent blog post."));
    }
}
