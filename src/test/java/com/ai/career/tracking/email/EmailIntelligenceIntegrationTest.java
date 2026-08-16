package com.ai.career.tracking.email;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.tracking.domain.repository.ApplicationActivityRepository;
import com.ai.career.tracking.email.domain.repository.EmailClassificationResultRepository;
import com.ai.career.tracking.email.domain.repository.EmailMessageRepository;
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
class EmailIntelligenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationActivityRepository activityRepository;

    @Autowired
    private EmailMessageRepository emailMessageRepository;

    @Autowired
    private EmailClassificationResultRepository classificationResultRepository;

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

        classificationResultRepository.deleteAll();
        emailMessageRepository.deleteAll();
        activityRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("email-intel-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        job = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("EM-JOB-101-" + System.currentTimeMillis())
                .title("Senior Cloud Architect")
                .company("Apex Systems")
                .url("https://example.com/jobs/apex-101")
                .description("Build cloud native Java microservices")
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
    void testSimulatedEmailIngestionAndAutoMatching() throws Exception {
        // 1. Simulate an interview invitation email from Apex Systems
        String simulateJson = objectMapper.writeValueAsString(Map.of(
                "sender", "recruiter@apexsystems.com",
                "subject", "Invitation to Interview for Senior Cloud Architect at Apex Systems",
                "bodySnippet", "Hi, we would love to schedule an interview with you. Please select a time on Zoom: https://zoom.us/j/999888777",
                "externalThreadId", "thread-apex-1"
        ));

        String simulateResp = mockMvc.perform(post("/api/v1/email-intelligence/simulate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simulateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.classification").value("INTERVIEW_INVITATION"))
                .andExpect(jsonPath("$.data.applicationId").value(application.getId()))
                .andReturn().getResponse().getContentAsString();

        Long emailId = objectMapper.readTree(simulateResp).get("data").get("id").asLong();

        // 2. Verify Application state was updated to INTERVIEW
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/tracking")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("INTERVIEW"))
                .andExpect(jsonPath("$.data.nextActionDecision.nextAction").value("PREPARE_FOR_INTERVIEW"));

        // 3. Get User Emails
        mockMvc.perform(get("/api/v1/email-intelligence")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].id").value(emailId));

        // 4. Get Email Details
        mockMvc.perform(get("/api/v1/email-intelligence/" + emailId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(emailId))
                .andExpect(jsonPath("$.data.senderDomain").value("apexsystems.com"));
    }
}
