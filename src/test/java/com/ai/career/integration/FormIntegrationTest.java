package com.ai.career.integration;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.auth.dto.RegisterRequest;
import com.ai.career.config.TestRedisConfig;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@Transactional
public class FormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    public void testFormDiscoveryClassificationAndMappingFlow() throws Exception {
        // 1. Register candidate user
        RegisterRequest regReq = RegisterRequest.builder()
            .email("form.candidate." + System.currentTimeMillis() + "@example.com")
            .password("Password123!")
            .fullName("Form Test Candidate")
            .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();
        User user = userRepository.findByEmail(regReq.getEmail()).orElseThrow();

        // 2. Create job and application
        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M4-JOB-" + System.currentTimeMillis())
            .title("Senior Java Engineer")
            .company("Tech Corp")
            .location("Remote")
            .description("Build microservices")
            .url("https://example.com/apply")
            .build());

        Application app = applicationRepository.save(Application.builder()
            .user(user)
            .job(job)
            .status(ApplicationState.PREPARING)
            .providerName("MOCK")
            .build());

        // 3. POST /api/v1/applications/{id}/form/analyze
        mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/form/analyze")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationId").value(app.getId()))
            .andExpect(jsonPath("$.data.fields").isArray())
            .andExpect(jsonPath("$.data.mappings").isArray())
            .andExpect(jsonPath("$.data.readinessStatus").exists());

        // 4. GET /api/v1/applications/{id}/form/plan
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/form/plan")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationId").value(app.getId()));

        // 5. POST /api/v1/applications/{id}/form/approve
        mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/form/approve")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.readinessStatus").value("READY"));

        // Verify application status transitioned to READY_FOR_REVIEW
        Application reloaded = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationState.READY_FOR_REVIEW, reloaded.getStatus());
    }
}
