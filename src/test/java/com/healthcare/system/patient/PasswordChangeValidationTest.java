package com.healthcare.system.patient;

import com.healthcare.system.dto.patient.PasswordChangeRequest;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.BadRequestException;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.repository.UserRepository;
import com.healthcare.system.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordChangeValidationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("patient.pwd@apexcare.health", "encodedCurrentPass", "Tom", "Riddle", "123", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        testUser.setId(5L);
    }

    @Test
    @DisplayName("Password Change: Rejects when current password does not match")
    void shouldRejectWhenCurrentPasswordIsIncorrect() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongCurrent", "encodedCurrentPass")).thenReturn(false);

        PasswordChangeRequest request = new PasswordChangeRequest("wrongCurrent", "newPass123", "newPass123");

        assertThatThrownBy(() -> patientService.changePassword(5L, request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Current password provided does not match our records.");
    }

    @Test
    @DisplayName("Password Change: Rejects when new password and confirm password do not match")
    void shouldRejectWhenNewPasswordAndConfirmMismatch() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctCurrent", "encodedCurrentPass")).thenReturn(true);

        PasswordChangeRequest request = new PasswordChangeRequest("correctCurrent", "newPass123", "differentConfirm");

        assertThatThrownBy(() -> patientService.changePassword(5L, request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("New password and confirm password do not match.");
    }

    @Test
    @DisplayName("Password Change: Updates password successfully on valid request")
    void shouldChangePasswordSuccessfully() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctCurrent", "encodedCurrentPass")).thenReturn(true);
        when(passwordEncoder.matches("brandNewPass123", "encodedCurrentPass")).thenReturn(false);
        when(passwordEncoder.encode("brandNewPass123")).thenReturn("encodedBrandNewPass123");

        PasswordChangeRequest request = new PasswordChangeRequest("correctCurrent", "brandNewPass123", "brandNewPass123");

        patientService.changePassword(5L, request);

        verify(userRepository).save(testUser);
    }
}
