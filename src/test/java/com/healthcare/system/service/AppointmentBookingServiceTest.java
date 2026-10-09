package com.healthcare.system.service;

import com.healthcare.system.dto.appointment.AppointmentBookingRequest;
import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.*;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.BadRequestException;
import com.healthcare.system.exception.ConflictException;
import com.healthcare.system.repository.AppointmentRepository;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.repository.DoctorScheduleRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        User patUser = new User("patient.book@apexcare.health", "pw", "John", "Watson", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE);
        patUser.setId(10L);
        patient = new Patient(patUser, LocalDate.of(1992, 1, 1), "Male", "B+", "111-222", "Baker St");
        patient.setId(1L);

        User docUser = new User("doctor.book@apexcare.health", "pw", "Sherlock", "Holmes", "222", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        docUser.setId(20L);
        doctor = new Doctor(docUser, "Consulting Detective", "MD", 10, BigDecimal.valueOf(100), "Consulting");
        doctor.setId(2L);

        schedule = new DoctorSchedule(doctor, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 30, true);
    }

    @Test
    @DisplayName("Appointment Booking: Successfully books slot when available within working schedule")
    void shouldBookAppointmentSuccessfully() {
        LocalDate friday = LocalDate.now().plusWeeks(1);
        while (friday.getDayOfWeek() != DayOfWeek.FRIDAY) {
            friday = friday.plusDays(1);
        }

        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDayOfWeek(2L, DayOfWeek.FRIDAY)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(2L, friday, LocalTime.of(10, 0))).thenReturn(false);

        Appointment savedAppointment = new Appointment(
            patient, doctor, friday, LocalTime.of(10, 0), LocalTime.of(10, 30),
            AppointmentStatus.CONFIRMED, "Routine Checkup", null
        );
        savedAppointment.setId(100L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentBookingRequest request = new AppointmentBookingRequest(2L, friday, LocalTime.of(10, 0), "Routine Checkup", null);

        AppointmentResponse response = appointmentService.bookAppointment(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Appointment Booking: Rejects booking on past date")
    void shouldRejectBookingInPast() {
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));

        LocalDate pastDate = LocalDate.now().minusDays(1);
        AppointmentBookingRequest request = new AppointmentBookingRequest(2L, pastDate, LocalTime.of(10, 0), "Reason", null);

        assertThatThrownBy(() -> appointmentService.bookAppointment(10L, request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Appointment date cannot be in the past.");
    }

    @Test
    @DisplayName("Appointment Booking: Rejects booking when slot is already booked (conflict prevention)")
    void shouldRejectBookingWhenSlotIsAlreadyBooked() {
        LocalDate friday = LocalDate.now().plusWeeks(1);
        while (friday.getDayOfWeek() != DayOfWeek.FRIDAY) {
            friday = friday.plusDays(1);
        }

        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDayOfWeek(2L, DayOfWeek.FRIDAY)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(2L, friday, LocalTime.of(10, 0))).thenReturn(true);

        AppointmentBookingRequest request = new AppointmentBookingRequest(2L, friday, LocalTime.of(10, 0), "Reason", null);

        assertThatThrownBy(() -> appointmentService.bookAppointment(10L, request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("The selected time slot is already booked.");
    }
}
