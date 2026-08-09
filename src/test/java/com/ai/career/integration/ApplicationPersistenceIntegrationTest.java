package com.ai.career.integration;

import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.dto.ApplicationResponse;
import com.ai.career.application.dto.CreateApplicationRequest;
import com.ai.career.application.dto.TransitionStateRequest;
import com.ai.career.application.service.ApplicationService;
import com.ai.career.auth.dto.RegisterRequest;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.repository.JobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ApplicationPersistenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationService applicationService;

    @Test
    public void testApplicationPersistenceAndDeduplication() throws Exception {
        // 1. Register candidate user
        RegisterRequest regReq = RegisterRequest.builder()
            .email("phase3.m1.candidate@example.com")
            .password("Password123!")
            .fullName("Phase3 Candidate")
            .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();

        // 2. Create job posting
        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M1-JOB-001")
            .title("Principal Software Architect")
            .company("Future Tech AI")
            .location("Remote")
            .description("Architect scalable cloud applications.")
            .url("https://example.com/p3-m1")
            .build());

        // 3. Create Application via REST API
        CreateApplicationRequest createReq = CreateApplicationRequest.builder()
            .jobId(job.getId())
            .matchScore(new BigDecimal("92.50"))
            .atsScore(new BigDecimal("88.00"))
            .recommendation("STRONG_APPLY")
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.status").value("DISCOVERED"))
            .andReturn();

        Long applicationId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 4. Verify Active Application Deduplication prevents duplicate active creation
        assertThrows(IllegalStateException.class, () -> {
            applicationService.createApplication(1L, CreateApplicationRequest.builder().jobId(job.getId()).build());
        });

        // 5. Transition Application State (DISCOVERED -> QUALIFIED -> PREPARING -> READY_FOR_REVIEW)
        TransitionStateRequest trans1 = TransitionStateRequest.builder()
            .targetState(ApplicationState.QUALIFIED)
            .reason("Qualified by eligibility engine")
            .build();

        mockMvc.perform(post("/api/v1/applications/" + applicationId + "/transition")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trans1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("QUALIFIED"));

        // 6. Verify Illegal State Transition fails with 500/exception (QUALIFIED -> APPLIED)
        TransitionStateRequest illegalTrans = TransitionStateRequest.builder()
            .targetState(ApplicationState.APPLIED)
            .build();

        mockMvc.perform(post("/api/v1/applications/" + applicationId + "/transition")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(illegalTrans)))
            .andExpect(status().isInternalServerError());

        // 7. Verify Application History API returns state transition log
        mockMvc.perform(get("/api/v1/applications/" + applicationId + "/history")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].toStatus").value("QUALIFIED"));
    }
}
