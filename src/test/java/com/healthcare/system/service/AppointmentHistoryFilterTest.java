package com.healthcare.system.service;

import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.Appointment;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentHistoryFilterTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        User pUser = new User("history.patient@apex.com", "pw", "Bruce", "Wayne", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        pUser.setId(10L);
        patient = new Patient(pUser, LocalDate.of(1985, 2, 19), "Male", "O-", "999", "Wayne Manor");
        patient.setId(1L);

        User dUser = new User("history.doctor@apex.com", "pw", "Lucius", "Fox", "222", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        dUser.setId(20L);
        doctor = new Doctor(dUser, "Cardiology", "MD", 20, BigDecimal.valueOf(300), "Cardiology");
        doctor.setId(2L);
    }

    @Test
    @DisplayName("Appointment History: Retrieves paginated appointments for the patient")
    void shouldRetrievePatientAppointmentsWithPagination() {
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));

        Appointment appt = new Appointment(
            patient, doctor, LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(14, 30),
            AppointmentStatus.CONFIRMED, "Cardio review", "Routine"
        );
        appt.setId(50L);

        Page<Appointment> page = new PageImpl<>(List.of(appt));
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<AppointmentResponse> result = appointmentService.getPatientAppointments(10L, AppointmentStatus.CONFIRMED, "UPCOMING", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(50L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }
}
