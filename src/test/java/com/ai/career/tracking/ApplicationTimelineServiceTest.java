package com.ai.career.tracking;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.User;
import com.ai.career.tracking.domain.entity.ApplicationActivity;
import com.ai.career.tracking.domain.repository.ApplicationActivityRepository;
import com.ai.career.tracking.dto.ActivityDto;
import com.ai.career.tracking.event.ApplicationActivityRecordedEvent;
import com.ai.career.tracking.dto.RecordActivityRequest;
import com.ai.career.tracking.service.impl.ApplicationTimelineServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationTimelineServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationActivityRepository activityRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationTimelineServiceImpl timelineService;

    private User user;
    private Application application;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).email("candidate@example.com").build();
        application = Application.builder().id(10L).user(user).build();
        timelineService = new ApplicationTimelineServiceImpl(applicationRepository, activityRepository, new ObjectMapper(), eventPublisher);
    }

    @Test
    void testRecordActivitySuccess() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));
        when(activityRepository.save(any(ApplicationActivity.class))).thenAnswer(inv -> {
            ApplicationActivity act = inv.getArgument(0);
            act.setId(500L);
            act.setCreatedAt(LocalDateTime.now());
            return act;
        });

        ActivityDto dto = timelineService.recordActivity(
                1L,
                10L,
                "CONFIRMATION_RECEIVED",
                "EMAIL",
                "Confirmation email received",
                Map.of("emailId", "msg-123"),
                0.95
        );

        assertNotNull(dto);
        assertEquals(500L, dto.getId());
        assertEquals("CONFIRMATION_RECEIVED", dto.getActivityType());
        assertEquals("EMAIL", dto.getSource());
        verify(activityRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(ApplicationActivityRecordedEvent.class));
    }

    @Test
    void testRecordActivityUnauthorized() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));

        assertThrows(IllegalArgumentException.class, () -> timelineService.recordActivity(
                999L,
                10L,
                "CONFIRMATION_RECEIVED",
                "EMAIL",
                "Test",
                null,
                1.0
        ));
    }

    @Test
    void testGetTimeline() {
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));
        ApplicationActivity act1 = ApplicationActivity.builder().id(1L).application(application).activityType("DISCOVERED").source("SYSTEM").createdAt(LocalDateTime.now().minusDays(2)).build();
        ApplicationActivity act2 = ApplicationActivity.builder().id(2L).application(application).activityType("APPLIED").source("SYSTEM").createdAt(LocalDateTime.now().minusDays(1)).build();

        when(activityRepository.findByApplicationIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(act1, act2));

        List<ActivityDto> timeline = timelineService.getTimeline(1L, 10L);

        assertNotNull(timeline);
        assertEquals(2, timeline.size());
        assertEquals("DISCOVERED", timeline.get(0).getActivityType());
        assertEquals("APPLIED", timeline.get(1).getActivityType());
    }
}
