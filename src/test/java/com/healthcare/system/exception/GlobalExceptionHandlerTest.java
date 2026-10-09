package com.healthcare.system.exception;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Exception Handler: Validates Bean Validation failure produces structured 400 response with field errors")
    void shouldReturnValidationErrorsOnInvalidRegistrationPayload() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email-format");
        invalidRequest.setPassword("123"); // Less than 6 chars
        invalidRequest.setFirstName(""); // Blank
        invalidRequest.setLastName(""); // Blank

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.data.email").exists())
            .andExpect(jsonPath("$.data.password").exists())
            .andExpect(jsonPath("$.data.firstName").exists())
            .andExpect(jsonPath("$.data.lastName").exists());
    }

    @Test
    @DisplayName("Exception Handler: Validates duplicate email triggers 409 Conflict with standardized error envelope")
    void shouldReturn409ConflictOnDuplicateEmailRegistration() throws Exception {
        RegisterRequest first = new RegisterRequest("dup.test@apexcare.health", "pass1234", "John", "Dup", "111", RoleType.ROLE_PATIENT);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest("dup.test@apexcare.health", "pass1234", "John", "Dup", "111", RoleType.ROLE_PATIENT);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("Exception Handler: Validates bad credentials return 401 Unauthorized envelope")
    void shouldReturn401OnBadCredentials() throws Exception {
        LoginRequest badLogin = new LoginRequest("nonexistent@apexcare.health", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badLogin)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("BAD_CREDENTIALS"));
    }
}
