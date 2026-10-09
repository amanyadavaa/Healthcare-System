package com.healthcare.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.system.dto.auth.LoginRequest;
import com.healthcare.system.dto.auth.RegisterRequest;
import com.healthcare.system.entity.enums.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Auth Security: Register, login, and verify protected endpoint access with JWT")
    void shouldRegisterLoginAndAccessProtectedMeEndpoint() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            "sarah.connor@apexcare.health",
            "SecurePass123!",
            "Sarah",
            "Connor",
            "+1-555-0199",
            RoleType.ROLE_PATIENT
        );

        // 1. Register
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.email").value("sarah.connor@apexcare.health"))
            .andReturn();

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("sarah.connor@apexcare.health", "SecurePass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        assertThat(token).isNotBlank();

        // 3. Access /api/auth/me with Bearer token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("sarah.connor@apexcare.health"))
            .andExpect(jsonPath("$.role").value("ROLE_PATIENT"));
    }

    @Test
    @DisplayName("Auth Security: Accessing protected endpoints without token returns 401 Unauthorized")
    void shouldReturn401WhenAccessingProtectedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }
}
