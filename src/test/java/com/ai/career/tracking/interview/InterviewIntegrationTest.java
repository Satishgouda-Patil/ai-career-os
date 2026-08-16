package com.ai.career.tracking.interview;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.tracking.interview.domain.repository.InterviewPreparationRepository;
import com.ai.career.tracking.interview.domain.repository.InterviewRepository;
import com.ai.career.tracking.interview.domain.repository.MockInterviewSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewPreparationRepository prepRepository;

    @Autowired
    private MockInterviewSessionRepository mockSessionRepository;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Job job;
    private Application application;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        mockSessionRepository.deleteAll();
        prepRepository.deleteAll();
        interviewRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("interview-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        job = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("INT-JOB-101-" + System.currentTimeMillis())
                .title("Lead Backend Architect")
                .company("CloudScale")
                .url("https://example.com/jobs/cs-101")
                .description("Build distributed cloud platforms in Java")
                .build());

        application = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(2))
                .build());

        String loginJson = objectMapper.writeValueAsString(Map.of(
                "email", user.getEmail(),
                "password", "Password123!"
        ));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void testScheduleInterviewPrepWorkspaceAndMockEvaluation() throws Exception {
        // 1. Schedule Interview
        String scheduleJson = objectMapper.writeValueAsString(Map.of(
                "interviewType", "TECHNICAL",
                "meetingUrl", "https://zoom.us/j/987654321",
                "interviewerName", "Alex Morgan",
                "interviewerTitle", "VP of Engineering"
        ));

        String createResp = mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/interviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scheduleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.interviewType").value("TECHNICAL"))
                .andExpect(jsonPath("$.data.companyName").value("CloudScale"))
                .andReturn().getResponse().getContentAsString();

        Long interviewId = objectMapper.readTree(createResp).get("data").get("id").asLong();

        // 2. Fetch Interview Prep Workspace
        mockMvc.perform(get("/api/v1/interviews/" + interviewId + "/prep")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyOverview.companyName").value("CloudScale"))
                .andExpect(jsonPath("$.data.sampleQuestions", hasSize(greaterThanOrEqualTo(1))));

        // 3. Generate Mock Practice Question
        String questionJson = objectMapper.writeValueAsString(Map.of("category", "TECHNICAL"));
        String mockQuestionResp = mockMvc.perform(post("/api/v1/interviews/" + interviewId + "/mock/question")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionCategory").value("TECHNICAL"))
                .andReturn().getResponse().getContentAsString();

        Long mockSessionId = objectMapper.readTree(mockQuestionResp).get("data").get("id").asLong();

        // 4. Evaluate Mock Answer
        String evalJson = objectMapper.writeValueAsString(Map.of(
                "mockSessionId", mockSessionId,
                "candidateAnswer", "I design microservice architectures using Spring Boot, database sharding, Redis caching, and RabbitMQ async messaging to ensure fault tolerance under heavy load."
        ));

        mockMvc.perform(post("/api/v1/interviews/" + interviewId + "/mock/evaluate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mockSessionId))
                .andExpect(jsonPath("$.data.score").value(greaterThanOrEqualTo(60)))
                .andExpect(jsonPath("$.data.feedback").exists());

        // 5. Get User Interviews
        mockMvc.perform(get("/api/v1/interviews")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
