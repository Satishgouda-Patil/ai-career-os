package com.ai.career.execution;

import com.ai.career.application.domain.entity.Application;
import com.ai.career.application.domain.entity.ApplicationState;
import com.ai.career.application.domain.repository.ApplicationRepository;
import com.ai.career.domain.entity.Job;
import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.JobRepository;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.dto.ExecuteApplicationRequest;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.execution.service.ApplicationOrchestratorService;
import com.ai.career.workspace.domain.entity.Workspace;
import com.ai.career.workspace.domain.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationOrchestrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ApplicationOrchestratorService orchestratorService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private DistributedExecutionLock distributedExecutionLock;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.when(distributedExecutionLock.acquire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(true);

        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("orchestration-user@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", user.getEmail(), "Password123!");
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void testReadinessAndApproveAndPrepareEndpoints() throws Exception {
        Job job = jobRepository.save(Job.builder()
                .source("JOOBLE")
                .sourceJobId("P3-M5-JOB-100")
                .title("Orchestrator Architect")
                .company("Cloud OS")
                .location("Remote")
                .description("Build M5 Orchestrator")
                .url("https://example.com/jobs/p3-m5-100")
                .build());

        workspaceRepository.save(Workspace.builder().user(user).job(job).build());

        Application application = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status(ApplicationState.READY_FOR_REVIEW)
                .providerName("GENERIC_JOB_FORM")
                .applicationUrl("https://example.com/jobs/p3-m5-100")
                .build());

        // 1. Get Readiness Endpoint
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/pipeline-readiness")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value(application.getId()))
                .andExpect(jsonPath("$.data.currentState").value("READY_FOR_REVIEW"));



        // 2. Approve and Prepare Endpoint
        mockMvc.perform(post("/api/v1/applications/" + application.getId() + "/approve-and-prepare")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Human approval granted for integration test\"}"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.applicationId").value(application.getId()));
        mockMvc.perform(get("/api/v1/applications/" + application.getId() + "/workflow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
