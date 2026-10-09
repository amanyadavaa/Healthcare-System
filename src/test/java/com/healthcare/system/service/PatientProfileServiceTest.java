package com.healthcare.system.service;

import com.healthcare.system.dto.patient.PatientProfileResponse;
import com.healthcare.system.dto.patient.PatientProfileUpdateRequest;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User sampleUser;
    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        sampleUser = new User("emma.watson@apexcare.health", "encodedPass", "Emma", "Watson", "555-1234", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        sampleUser.setId(10L);

        samplePatient = new Patient(sampleUser, LocalDate.of(1990, 4, 15), "Female", "A+", "555-9999", "456 Oak Avenue");
        samplePatient.setId(20L);
    }

    @Test
    @DisplayName("Patient Profile: Retrieves existing patient profile accurately")
    void shouldGetPatientProfileSuccessfully() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(samplePatient));

        PatientProfileResponse response = patientService.getPatientProfile(10L);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("emma.watson@apexcare.health");
        assertThat(response.getFirstName()).isEqualTo("Emma");
        assertThat(response.getBloodGroup()).isEqualTo("A+");
    }

    @Test
    @DisplayName("Patient Profile: Throws ResourceNotFoundException when user does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientProfile(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with id: '999'");
    }

    @Test
    @DisplayName("Patient Profile: Updates demographic and personal contact information")
    void shouldUpdatePatientProfileSuccessfully() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(samplePatient));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

        PatientProfileUpdateRequest updateRequest = new PatientProfileUpdateRequest(
            "Emma", "Watson-Brown", "555-0000",
            LocalDate.of(1990, 4, 15), "Female", "A+",
            "555-8888", "789 Pine Road"
        );

        PatientProfileResponse response = patientService.updatePatientProfile(10L, updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }
}
