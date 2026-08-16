package com.ai.career.tracking.interview;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.tracking.interview.domain.entity.Interview;
import com.ai.career.tracking.interview.domain.entity.MockInterviewSession;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import com.ai.career.tracking.interview.domain.repository.MockInterviewSessionRepository;
import com.ai.career.tracking.interview.dto.EvaluateAnswerRequest;
import com.ai.career.tracking.interview.dto.MockInterviewDto;
import com.ai.career.tracking.interview.service.MockInterviewService;
import com.ai.career.tracking.interview.service.impl.MockInterviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MockInterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private MockInterviewSessionRepository mockSessionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MockInterviewService mockInterviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockInterviewService = new MockInterviewServiceImpl(
                interviewRepository,
                mockSessionRepository,
                eventPublisher
        );
    }

    @Test
    void testEvaluateMockCandidateAnswer() {
        User user = User.builder().id(1L).build();
        Job job = Job.builder().company("Amazon").title("Software Development Engineer").build();
        Application app = Application.builder().id(40L).user(user).job(job).build();
        Interview interview = Interview.builder().id(400L).application(app).build();

        when(interviewRepository.findById(400L)).thenReturn(Optional.of(interview));
        when(mockSessionRepository.save(any(MockInterviewSession.class))).thenAnswer(inv -> {
            MockInterviewSession s = inv.getArgument(0);
            s.setId(450L);
            return s;
        });

        EvaluateAnswerRequest request = EvaluateAnswerRequest.builder()
                .question("How do you design a high-throughput microservice system?")
                .candidateAnswer("I design microservices by decoupling components with RabbitMQ queues, implementing Redis caching for frequent queries, using Spring Boot REST controllers, and setting up database indexing to minimize latency.")
                .questionCategory("TECHNICAL")
                .build();

        MockInterviewDto evaluated = mockInterviewService.evaluateCandidateAnswer(1L, 400L, request);

        assertNotNull(evaluated);
        assertEquals(450L, evaluated.getId());
        assertTrue(evaluated.getScore() >= 65);
        assertNotNull(evaluated.getFeedback());
        assertNotNull(evaluated.getImprovedAnswer());
    }
}
