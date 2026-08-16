package com.ai.career.tracking.interview;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.ProfileRepository;
import com.ai.career.tracking.interview.domain.entity.Interview;
import com.ai.career.tracking.interview.domain.entity.InterviewPreparation;
import com.ai.career.tracking.interview.domain.repository.InterviewPreparationRepository;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import com.ai.career.tracking.interview.dto.InterviewDto;
import com.ai.career.tracking.interview.dto.InterviewPrepDto;
import com.ai.career.tracking.interview.dto.ScheduleInterviewRequest;
import com.ai.career.tracking.interview.service.InterviewPrepService;
import com.ai.career.tracking.interview.service.impl.InterviewPrepServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterviewPrepServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewPreparationRepository prepRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InterviewPrepService prepService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prepService = new InterviewPrepServiceImpl(
                applicationRepository,
                interviewRepository,
                prepRepository,
                profileRepository,
                eventPublisher,
                new ObjectMapper()
        );
    }

    @Test
    void testCreateInterviewAndGeneratePrepWorkspace() {
        User user = User.builder().id(1L).email("user@example.com").build();
        Job job = Job.builder().company("Meta").title("Senior Systems Engineer").build();
        Application app = Application.builder().id(30L).user(user).job(job).status(ApplicationState.APPLIED).build();

        when(applicationRepository.findById(30L)).thenReturn(Optional.of(app));

        Interview savedInterview = Interview.builder()
                .id(300L)
                .application(app)
                .interviewType("TECHNICAL")
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .status("SCHEDULED")
                .build();

        when(interviewRepository.save(any(Interview.class))).thenReturn(savedInterview);
        when(interviewRepository.findById(300L)).thenReturn(Optional.of(savedInterview));

        when(prepRepository.save(any(InterviewPreparation.class))).thenAnswer(inv -> {
            InterviewPreparation p = inv.getArgument(0);
            p.setId(350L);
            return p;
        });

        ScheduleInterviewRequest request = ScheduleInterviewRequest.builder()
                .interviewType("TECHNICAL")
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .meetingUrl("https://zoom.us/j/12345")
                .build();

        InterviewDto result = prepService.createInterview(1L, 30L, request);

        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertEquals("TECHNICAL", result.getInterviewType());
        verify(prepRepository, times(1)).save(any(InterviewPreparation.class));
    }
}
