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
public class ValidationIntegrationTest {

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
    public void testValidationDryRunAndAuthorizationRestFlow() throws Exception {
        // 1. Register candidate user
        RegisterRequest regReq = RegisterRequest.builder()
            .email("val.candidate." + System.currentTimeMillis() + "@example.com")
            .password("Password123!")
            .fullName("Validation Candidate")
            .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();
        User user = userRepository.findByEmail(regReq.getEmail()).orElseThrow();

        // 2. Create job & application in READY_FOR_REVIEW state
        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M5-JOB-" + System.currentTimeMillis())
            .title("Lead Backend Architect")
            .company("Scale Tech")
            .location("Remote")
            .description("Build high scale services")
            .url("https://example.com/apply")
            .build());

        Application app = applicationRepository.save(Application.builder()
            .user(user)
            .job(job)
            .status(ApplicationState.READY_FOR_REVIEW)
            .providerName("MOCK")
            .build());

        // 3. Analyze form first
        mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/form/analyze")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // 4. POST /api/v1/applications/{id}/validate
        mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/validate")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicationId").value(app.getId()))
            .andExpect(jsonPath("$.data.status").exists())
            .andExpect(jsonPath("$.data.readiness").exists());

        // 5. POST /api/v1/applications/{id}/dry-run
        MvcResult dryRunRes = mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/dry-run")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.runId").exists())
            .andExpect(jsonPath("$.data.simulatedOutcome").value("DRY_RUN_SIMULATION_SUCCESS"))
            .andReturn();

        String runId = objectMapper.readTree(dryRunRes.getResponse().getContentAsString()).get("data").get("runId").asText();

        // 6. GET /api/v1/applications/{id}/dry-run/{runId}
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/dry-run/" + runId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.runId").value(runId));

        // 7. GET /api/v1/applications/{id}/readiness
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/readiness")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // 8. POST /api/v1/applications/{id}/authorize-execution
        mockMvc.perform(post("/api/v1/applications/" + app.getId() + "/authorize-execution")
                .header("Authorization", "Bearer " + token)
                .param("note", "Candidate approved dry run"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.executionAuthorized").value(true));

        // Verify application status transitioned to APPROVED
        Application reloaded = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationState.APPROVED, reloaded.getStatus());
    }
}
