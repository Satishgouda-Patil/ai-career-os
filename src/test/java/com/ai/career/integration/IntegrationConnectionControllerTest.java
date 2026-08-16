package com.ai.career.integration;

import com.ai.career.domain.entity.User;
import com.ai.career.domain.repository.UserRepository;
import com.ai.career.execution.lock.DistributedExecutionLock;
import com.ai.career.integration.domain.entity.IntegrationConnection;
import com.ai.career.integration.domain.repository.IntegrationConnectionRepository;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IntegrationConnectionRepository connectionRepository;

    @MockBean
    private DistributedExecutionLock distributedExecutionLock;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        when(distributedExecutionLock.acquire(anyString(), anyString(), anyLong())).thenReturn(true);

        connectionRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("conn-user-" + System.currentTimeMillis() + "@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build());

        connectionRepository.save(IntegrationConnection.builder()
                .user(user)
                .provider("IMAP_OAUTH_READONLY")
                .status("CONNECTED")
                .externalAccountId("candidate@example.com")
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
    void testGetIntegrationConnections() throws Exception {
        mockMvc.perform(get("/api/v1/integrations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].provider").value("IMAP_OAUTH_READONLY"))
                .andExpect(jsonPath("$.data[0].status").value("CONNECTED"));
    }
}
