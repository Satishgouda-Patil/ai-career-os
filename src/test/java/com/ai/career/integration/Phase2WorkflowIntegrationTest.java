package com.ai.career.integration;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class Phase2WorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    @Test
    public void testFullPhase2Workflow() throws Exception {
        // 1. Register candidate
        RegisterRequest regReq = RegisterRequest.builder()
            .email("phase2.candidate@example.com")
            .password("Password123!")
            .fullName("Phase2 Candidate")
            .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        String responseStr = regResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseStr).get("token").asText();

        // 2. Create target job
        Job job = Job.builder()
            .source("JOOBLE")
            .sourceJobId("PHASE2-JOB-001")
            .title("Staff Java Engineer")
            .company("Acme AI Corp")
            .location("San Francisco, CA")
            .description("Looking for a Staff Java Engineer with Spring Boot and Kafka experience.")
            .url("https://example.com/job/001")
            .build();
        Job savedJob = jobRepository.save(job);

        // 3. Test Resume Generation API
        mockMvc.perform(post("/api/v1/resume/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":" + savedJob.getId() + ",\"template\":\"MODERN\"}"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists());

        // 4. Test Deep Job Analysis API
        mockMvc.perform(post("/api/v1/jobs/" + savedJob.getId() + "/analyze")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));

        // 5. Test Cover Letter Generation API
        mockMvc.perform(post("/api/v1/cover-letter/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":" + savedJob.getId() + ",\"tone\":\"Professional\"}"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.success").value(true));

        // 6. Test AI Workspace API
        mockMvc.perform(get("/api/v1/workspace/" + savedJob.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.workspaceId").exists());
    }
}
