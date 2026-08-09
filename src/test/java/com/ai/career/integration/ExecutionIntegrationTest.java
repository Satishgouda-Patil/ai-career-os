package com.ai.career.integration;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.auth.dto.RegisterRequest;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.ai.career.config.TestRedisConfig.class)
@Transactional
public class ExecutionIntegrationTest {

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
    public void testMockExecutionFlowAndStateSafety() throws Exception {
        // 1. Register candidate user
        RegisterRequest regReq = RegisterRequest.builder()
            .email("execution.candidate@example.com")
            .password("Password123!")
            .fullName("Execution Candidate")
            .build();

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
            .andExpect(status().isCreated())
            .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("token").asText();
        User user = userRepository.findByEmail("execution.candidate@example.com").orElseThrow();

        // 2. Create Job
        Job job = jobRepository.save(Job.builder()
            .source("JOOBLE")
            .sourceJobId("P3-M2-JOB-100")
            .title("Senior Cloud Automation Engineer")
            .company("Cloud OS Inc")
            .location("Remote")
            .description("Build automated cloud infrastructure.")
            .url("https://example.com/jobs/p3-m2-100")
            .build());

        // 3. Create Application in APPROVED state
        Application application = applicationRepository.save(Application.builder()
            .user(user)
            .job(job)
            .status(ApplicationState.APPROVED)
            .providerName("MOCK")
            .applicationUrl("https://example.com/jobs/p3-m2-100")
            .build());

        // 4. Validate execution endpoint prior to submission
        mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/execute/validate")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.valid").value(true));

        // 5. Execute Mock Provider Application (SUCCESS scenario)
        ExecuteApplicationRequest execReq = ExecuteApplicationRequest.builder()
            .dryRun(false)
            .build();

        mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/execute")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(execReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.executionStatus").value("SUCCEEDED"))
            .andExpect(jsonPath("$.data.outcomeStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.data.applicationStatus").value("APPLIED"));

        // 6. Verify Execution History Endpoint
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/executions")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].providerName").value("MOCK"));

        // 7. Verify UNKNOWN Outcome State Safety Rule (Transitions to SUBMISSION_REQUIRES_REVIEW, NEVER APPLIED automatically)
        Application appUnknown = applicationRepository.save(Application.builder()
            .user(user)
            .job(jobRepository.save(Job.builder().source("JOOBLE").sourceJobId("P3-M2-JOB-200").title("DevOps Lead").company("Cloud OS").location("Remote").description("DevOps").url("https://example.com/200").build()))
            .status(ApplicationState.APPROVED)
            .providerName("MOCK")
            .build());

        ExecuteApplicationRequest unknownReq = ExecuteApplicationRequest.builder()
            .answers(Map.of("mockScenario", "UNKNOWN"))
            .build();

        mockMvc.perform(post("/api/v1/applications/" + appUnknown.getId() + "/execute")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unknownReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.executionStatus").value("UNKNOWN"))
            .andExpect(jsonPath("$.data.applicationStatus").value("SUBMISSION_REQUIRES_REVIEW"));
    }
}
