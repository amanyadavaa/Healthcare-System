package com.healthcare.system.service;

import com.healthcare.system.dto.medical.MedicalRecordResponse;
import com.healthcare.system.entity.*;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.MedicalRecordRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryAccessTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private Patient patientA;
    private Patient patientB;
    private Doctor doctor;
    private MedicalRecord recordForPatientA;

    @BeforeEach
    void setUp() {
        User userA = new User("patient.a@apex.com", "pw", "Alice", "Smith", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        userA.setId(1L);
        patientA = new Patient(userA, LocalDate.of(1995, 5, 10), "Female", "A+", "999", "101 Main St");
        patientA.setId(10L);

        User userB = new User("patient.b@apex.com", "pw", "Bob", "Jones", "222", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        userB.setId(2L);
        patientB = new Patient(userB, LocalDate.of(1990, 8, 20), "Male", "B+", "888", "202 Elm St");
        patientB.setId(20L);

        User docUser = new User("doctor@apex.com", "pw", "Doctor", "Watson", "333", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        docUser.setId(3L);
        doctor = new Doctor(docUser, "Internal Medicine", "MD", 10, BigDecimal.valueOf(120), "General");
        doctor.setId(30L);

        Appointment appt = new Appointment(patientA, doctor, LocalDate.now().minusDays(5), LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.COMPLETED, "Checkup", null);
        appt.setId(100L);

        recordForPatientA = new MedicalRecord(
            appt, patientA, doctor,
            "Acute Bronchitis",
            "Amoxicillin 500mg tid x 7d",
            "Rest and drink fluids",
            "120/80", "72 bpm", "98.6 F"
        );
        recordForPatientA.setId(500L);
    }

    @Test
    @DisplayName("Medical History: Patient views own medical record successfully")
    void shouldAllowPatientToViewOwnMedicalRecord() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patientA));
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(recordForPatientA));

        MedicalRecordResponse response = medicalRecordService.getMedicalRecordByIdForPatient(1L, 500L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getDiagnosis()).isEqualTo("Acute Bronchitis");
        assertThat(response.getPrescription()).contains("Amoxicillin");
        assertThat(response.getVitalsBp()).isEqualTo("120/80");
    }

    @Test
    @DisplayName("IDOR Defense: Patient is blocked with AccessDeniedException when accessing another patient's medical record")
    void shouldBlockPatientFromAccessingAnotherPatientsRecord() {
        when(patientRepository.findByUserId(2L)).thenReturn(Optional.of(patientB));
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(recordForPatientA));

        assertThatThrownBy(() -> medicalRecordService.getMedicalRecordByIdForPatient(2L, 500L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Access denied: You are not authorized to view this medical record.");
    }

    @Test
    @DisplayName("Medical History: Throws ResourceNotFoundException when record ID does not exist")
    void shouldThrowExceptionWhenRecordNotFound() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patientA));
        when(medicalRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.getMedicalRecordByIdForPatient(1L, 999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("MedicalRecord not found with id: '999'");
    }
}
