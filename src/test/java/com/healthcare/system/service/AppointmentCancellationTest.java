package com.healthcare.system.service;

import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.Appointment;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.BadRequestException;
import com.healthcare.system.repository.AppointmentRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.service.impl.AppointmentServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentCancellationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Patient otherPatient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        User u1 = new User("patient1@apex.com", "pw", "Clark", "Kent", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        u1.setId(1L);
        patient = new Patient(u1, LocalDate.of(1980, 2, 29), "Male", "O+", "999", "Smallville");
        patient.setId(10L);

        User u2 = new User("patient2@apex.com", "pw", "Lois", "Lane", "222", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        u2.setId(2L);
        otherPatient = new Patient(u2, LocalDate.of(1982, 3, 1), "Female", "A-", "888", "Metropolis");
        otherPatient.setId(20L);

        User dUser = new User("doc@apex.com", "pw", "Doctor", "Who", "333", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        dUser.setId(3L);
        doctor = new Doctor(dUser, "General", "MD", 900, BigDecimal.valueOf(50), "Time");
        doctor.setId(30L);
    }

    @Test
    @DisplayName("Appointment Cancellation: Successfully cancels future appointment with reason")
    void shouldCancelFutureAppointmentSuccessfully() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        Appointment appt = new Appointment(
            patient, doctor, LocalDate.now().plusDays(3), LocalTime.of(10, 0), LocalTime.of(10, 30),
            AppointmentStatus.CONFIRMED, "Checkup", null
        );
        appt.setId(100L);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appt));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appt);

        AppointmentResponse response = appointmentService.cancelAppointmentByPatient(1L, 100L, "Travel conflicts");

        assertThat(response).isNotNull();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appt.getNotes()).contains("Travel conflicts");
        verify(appointmentRepository).save(appt);
    }

    @Test
    @DisplayName("Appointment Cancellation: Rejects cancellation if patient is not the appointment owner")
    void shouldRejectCancellationIfNotAppointmentOwner() {
        when(patientRepository.findByUserId(2L)).thenReturn(Optional.of(otherPatient));

        // Appointment belongs to patient 1, requested by patient 2
        Appointment appt = new Appointment(
            patient, doctor, LocalDate.now().plusDays(3), LocalTime.of(10, 0), LocalTime.of(10, 30),
            AppointmentStatus.CONFIRMED, "Checkup", null
        );
        appt.setId(100L);
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> appointmentService.cancelAppointmentByPatient(2L, 100L, "Sneaky cancel"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("You do not have authorization to cancel this appointment.");
    }

    @Test
    @DisplayName("Appointment Cancellation: Rejects cancelling already completed appointment")
    void shouldRejectCancellingCompletedAppointment() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        Appointment completedAppt = new Appointment(
            patient, doctor, LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
            AppointmentStatus.COMPLETED, "Checkup", null
        );
        completedAppt.setId(101L);
        when(appointmentRepository.findById(101L)).thenReturn(Optional.of(completedAppt));

        assertThatThrownBy(() -> appointmentService.cancelAppointmentByPatient(1L, 101L, "Cancel past"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Completed appointments cannot be cancelled.");
    }
}
