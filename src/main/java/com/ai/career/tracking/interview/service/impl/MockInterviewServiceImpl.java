package com.ai.career.tracking.interview.service.impl;

import com.ai.career.tracking.interview.domain.entity.Interview;
import com.ai.career.tracking.interview.domain.entity.MockInterviewSession;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import com.ai.career.tracking.interview.domain.repository.MockInterviewSessionRepository;
import com.ai.career.tracking.interview.dto.EvaluateAnswerRequest;
import com.ai.career.tracking.interview.dto.MockInterviewDto;
import com.ai.career.tracking.interview.event.MockInterviewEvaluatedEvent;
import com.ai.career.tracking.interview.service.MockInterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockInterviewServiceImpl implements MockInterviewService {

    private final InterviewRepository interviewRepository;
    private final MockInterviewSessionRepository mockSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MockInterviewDto generatePracticeQuestion(Long userId, Long interviewId, String category) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        String cat = (category != null && !category.isBlank()) ? category.toUpperCase() : "TECHNICAL";
        String company = interview.getApplication().getJob() != null ? interview.getApplication().getJob().getCompany() : "the company";
        String title = interview.getApplication().getJob() != null ? interview.getApplication().getJob().getTitle() : "Software Engineer";

        String question;
        if ("BEHAVIORAL".equalsIgnoreCase(cat)) {
            question = "Describe a situation where a project deadline was at risk. How did you prioritize tasks and communicate with stakeholders?";
        } else if ("COMPANY_SPECIFIC".equalsIgnoreCase(cat)) {
            question = "What architectural challenges do you foresee when scaling systems at " + company + ", and how would you address them?";
        } else if ("ROLE_SPECIFIC".equalsIgnoreCase(cat)) {
            question = "Walk me through how you design high-throughput REST APIs and microservices for a " + title + " position.";
        } else {
            question = "Explain how you handle distributed caching, database indexing, and transaction consistency in microservice architecture.";
        }

        MockInterviewSession session = MockInterviewSession.builder()
                .interview(interview)
                .question(question)
                .questionCategory(cat)
                .build();

        session = mockSessionRepository.save(session);
        log.info("Generated practice question for Interview ID {}, Category {}: '{}'", interviewId, cat, question);

        return mapToDto(session);
    }

    @Override
    @Transactional
    public MockInterviewDto evaluateCandidateAnswer(Long userId, Long interviewId, EvaluateAnswerRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        MockInterviewSession session = null;
        if (request != null && request.getMockSessionId() != null) {
            session = mockSessionRepository.findById(request.getMockSessionId()).orElse(null);
        }

        String question = (request != null && request.getQuestion() != null) ? request.getQuestion() :
                (session != null ? session.getQuestion() : "General technical interview question");
        String answer = (request != null && request.getCandidateAnswer() != null) ? request.getCandidateAnswer().trim() : "";
        String cat = (request != null && request.getQuestionCategory() != null) ? request.getQuestionCategory() :
                (session != null ? session.getQuestionCategory() : "TECHNICAL");

        // Compute AI evaluation score & feedback
        int score = calculateScore(answer);
        String feedback = generateFeedback(score, answer, cat);
        String improvedAnswer = generateImprovedAnswer(question, answer, cat);

        if (session == null) {
            session = MockInterviewSession.builder()
                    .interview(interview)
                    .question(question)
                    .questionCategory(cat)
                    .candidateAnswer(answer)
                    .score(score)
                    .feedback(feedback)
                    .improvedAnswer(improvedAnswer)
                    .evaluatedAt(LocalDateTime.now())
                    .build();
        } else {
            session.setCandidateAnswer(answer);
            session.setScore(score);
            session.setFeedback(feedback);
            session.setImprovedAnswer(improvedAnswer);
            session.setEvaluatedAt(LocalDateTime.now());
        }

        session = mockSessionRepository.save(session);
        log.info("Evaluated mock interview session ID {}, Score: {}", session.getId(), score);

        eventPublisher.publishEvent(MockInterviewEvaluatedEvent.builder()
                .interviewId(interviewId)
                .sessionId(session.getId())
                .applicationId(interview.getApplication().getId())
                .userId(userId)
                .score(score)
                .correlationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToDto(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MockInterviewDto> getInterviewMockSessions(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found with ID: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to interview ID: " + interviewId);
        }

        return mockSessionRepository.findByInterviewIdOrderByCreatedAtDesc(interviewId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private int calculateScore(String answer) {
        if (answer == null || answer.isBlank()) return 0;
        int words = answer.split("\\s+").length;
        if (words < 15) return 40;
        if (words < 40) return 65;
        if (words < 80) return 85;
        return 95;
    }

    private String generateFeedback(int score, String answer, String cat) {
        if (score < 50) {
            return "Your response is too brief. Expand your answer with specific metrics, technical trade-offs, and clear structure (Situation, Task, Action, Result for behavioral questions).";
        } else if (score < 80) {
            return "Good response foundation. To elevate your score above 90, explicitly quantify your impact (e.g., 'reduced latency by 30%') and explain the architectural reasoning behind your decisions.";
        } else {
            return "Excellent, structured response! You effectively covered technical trade-offs, practical implementation steps, and business outcomes.";
        }
    }

    private String generateImprovedAnswer(String question, String answer, String cat) {
        StringBuilder sb = new StringBuilder();
        sb.append("Strong Refined Answer Template:\n\n")
          .append("1. Context & Objective: Briefly state the situation and core engineering challenge.\n")
          .append("2. Action & Decision: Detail your technical approach, specific design choices, and trade-offs.\n")
          .append("3. Quantified Outcome: Conclude with measurable results (e.g., performance improvement, reliability metrics, team velocity gain).\n\n");
        if (answer != null && !answer.isBlank()) {
            sb.append("Your response highlights: ").append(answer);
        }
        return sb.toString();
    }

    private MockInterviewDto mapToDto(MockInterviewSession session) {
        return MockInterviewDto.builder()
                .id(session.getId())
                .interviewId(session.getInterview().getId())
                .question(session.getQuestion())
                .questionCategory(session.getQuestionCategory())
                .candidateAnswer(session.getCandidateAnswer())
                .score(session.getScore())
                .feedback(session.getFeedback())
                .improvedAnswer(session.getImprovedAnswer())
                .evaluatedAt(session.getEvaluatedAt())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
