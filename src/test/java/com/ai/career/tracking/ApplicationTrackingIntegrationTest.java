package com.ai.career.tracking;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.tracking.domain.repository.ApplicationActivityRepository;

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
class ApplicationTrackingIntegrationTest {

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

        activityRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("tracking-test-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        job = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("job-101-" + System.currentTimeMillis())
                .title("Senior Backend Engineer")
                .company("Acme AI Corp")
                .url("https://example.com/jobs/backend-101")
                .description("Build high scale Java microservices")
                .build());

        application = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(4))
                .build());

        // Authenticate to get JWT token
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
    void testTrackingEndpointsFlow() throws Exception {
        // 1. Record an activity
        String recordActivityJson = objectMapper.writeValueAsString(Map.of(
                "activityType", "CONFIRMATION_RECEIVED",
                "source", "EMAIL",
                "description", "Received confirmation email from recruiter",
                "metadata", Map.of("emailId", "msg-999"),
                "confidence", 0.98
        ));

        mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordActivityJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityType").value("CONFIRMATION_RECEIVED"))
                .andExpect(jsonPath("$.data.source").value("EMAIL"));

        // 2. Get Timeline
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/timeline")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].activityType").value("CONFIRMATION_RECEIVED"));

        // 3. Get Next Action
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/next-action")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextAction").value("FOLLOW_UP_DUE"))
                .andExpect(jsonPath("$.data.urgency").value("HIGH"));

        // 4. Get Tracking Summary
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/tracking")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value(application.getId()))
                .andExpect(jsonPath("$.data.company").value("Acme AI Corp"))
                .andExpect(jsonPath("$.data.currentStatus").value("APPLIED"))
                .andExpect(jsonPath("$.data.nextActionDecision.nextAction").value("FOLLOW_UP_DUE"))
                .andExpect(jsonPath("$.data.timeline", hasSize(greaterThanOrEqualTo(1))));
    }
}
