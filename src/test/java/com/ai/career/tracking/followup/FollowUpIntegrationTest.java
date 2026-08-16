package com.ai.career.tracking.followup;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.tracking.followup.domain.repository.ApplicationFollowUpRepository;
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
class FollowUpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationFollowUpRepository followUpRepository;

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

        followUpRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("followup-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        job = jobRepository.save(Job.builder()
                .source("LINKEDIN")
                .sourceJobId("FU-JOB-101-" + System.currentTimeMillis())
                .title("Principal Security Engineer")
                .company("CyberGuard")
                .url("https://example.com/jobs/cg-101")
                .description("Lead security operations")
                .build());

        application = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status(ApplicationState.APPLIED)
                .submittedAt(LocalDateTime.now().minusDays(3))
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
    void testFollowUpGenerationApproveAndSendLifecycle() throws Exception {
        // 1. Generate Follow-up Draft #1
        String genJson = objectMapper.writeValueAsString(Map.of(
                "sequenceNumber", 1,
                "customNotes", "Looking forward to hearing about security initiatives."
        ));

        String genResp = mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/follow-ups/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(genJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sequenceNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.followUpSubject").value(containsString("CyberGuard")))
                .andReturn().getResponse().getContentAsString();

        Long followUpId = objectMapper.readTree(genResp).get("data").get("id").asLong();

        // 2. Approve Follow-up
        mockMvc.perform(post("/api/v1/follow-ups/" + followUpId + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(followUpId))
                .andExpect(jsonPath("$.data.status").value("READY"));

        // 3. Send Follow-up
        mockMvc.perform(post("/api/v1/follow-ups/" + followUpId + "/send")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(followUpId))
                .andExpect(jsonPath("$.data.status").value("SENT"))
                .andExpect(jsonPath("$.data.sentAt").exists());

        // 4. Get Application Follow-ups
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/follow-ups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("SENT"));

        // 5. Get User Follow-ups
        mockMvc.perform(get("/api/v1/follow-ups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
