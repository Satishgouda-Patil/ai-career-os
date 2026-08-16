package com.ai.career.tracking.followup;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.User;
import com.ai.career.tracking.followup.domain.entity.ApplicationFollowUp;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
import com.ai.career.tracking.followup.dto.FollowUpDto;
import com.ai.career.tracking.followup.service.FollowUpGeneratorService;
import com.ai.career.tracking.followup.service.FollowUpSchedulerService;
import com.ai.career.tracking.followup.service.impl.FollowUpSchedulerServiceImpl;
import com.ai.career.tracking.service.ApplicationTimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpSchedulerServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationFollowUpRepository followUpRepository;

    @Mock
    private FollowUpGeneratorService generatorService;

    @Mock
    private ApplicationTimelineService timelineService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FollowUpSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schedulerService = new FollowUpSchedulerServiceImpl(
                applicationRepository,
                followUpRepository,
                generatorService,
                timelineService,
                eventPublisher
        );
    }

    @Test
    void testScheduleFirstFollowUp() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Application app = Application.builder().id(10L).user(user).status(ApplicationState.APPLIED).build();

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(app));
        when(followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(10L)).thenReturn(Collections.emptyList());

        ApplicationFollowUp savedEntity = ApplicationFollowUp.builder()
                .id(100L)
                .application(app)
                .channel("EMAIL")
                .sequenceNumber(1)
                .scheduledAt(LocalDateTime.now().plusDays(3))
                .status("SCHEDULED")
                .build();

        when(followUpRepository.save(any(ApplicationFollowUp.class))).thenReturn(savedEntity);
        when(followUpRepository.findById(100L)).thenReturn(Optional.of(savedEntity));

        FollowUpDto result = schedulerService.scheduleNextFollowUp(1L, 10L);

        assertNotNull(result);
        assertEquals(1, result.getSequenceNumber());
        assertEquals("SCHEDULED", result.getStatus());
        verify(generatorService, times(1)).generateFollowUpDraft(1L, 10L, 1, null);
    }

    @Test
    void testAutoCancelPendingFollowUpsOnRecruiterResponse() {
        User user = User.builder().id(1L).build();
        Application app = Application.builder().id(10L).user(user).build();
        ApplicationFollowUp followUp = ApplicationFollowUp.builder()
                .id(101L)
                .application(app)
                .status("SCHEDULED")
                .sequenceNumber(1)
                .build();

        when(followUpRepository.findByApplicationIdOrderBySequenceNumberAsc(10L)).thenReturn(List.of(followUp));

        schedulerService.autoCancelPendingFollowUps(10L, "Recruiter responded");

        assertEquals("CANCELLED", followUp.getStatus());
        verify(followUpRepository, times(1)).save(followUp);
        verify(eventPublisher, times(1)).publishEvent(any(com.ai.career.tracking.followup.event.ApplicationFollowUpCancelledEvent.class));
    }
}
