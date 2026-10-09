package com.healthcare.system.dto;

import com.healthcare.system.dto.auth.LoginRequest;
import com.healthcare.system.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("DTO Validation: LoginRequest fails on blank or invalid email")
    void shouldFailValidationOnInvalidLoginRequest() {
        LoginRequest request = new LoginRequest("", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("DTO Validation: RegisterRequest passes with complete valid payload")
    void shouldPassValidationOnValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest(
            "valid.user@apexcare.health",
            "password123",
            "John",
            "Doe",
            "1234567890",
            null
        );
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
