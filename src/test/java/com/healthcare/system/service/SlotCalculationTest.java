package com.healthcare.system.service;

import com.healthcare.system.dto.doctor.TimeSlotResponse;
import com.healthcare.system.entity.*;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.repository.AppointmentRepository;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.repository.DoctorScheduleRepository;
import com.healthcare.system.service.impl.DoctorDiscoveryServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotCalculationTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorDiscoveryServiceImpl doctorDiscoveryService;

    private Doctor doctor;
    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        User docUser = new User("dr.strange@apexcare.health", "pw", "Stephen", "Strange", "555", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        docUser.setId(100L);

        doctor = new Doctor(docUser, "Neurosurgeon", "MD", 12, BigDecimal.valueOf(250), "Neurology");
        doctor.setId(1L);

        // Schedule on Monday from 09:00 to 11:00 with 30-min slots -> 4 slots: 09:00, 09:30, 10:00, 10:30
        schedule = new DoctorSchedule(doctor, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), 30, true);
    }

    @Test
    @DisplayName("Slot Calculation: Generates all slots and marks occupied slot as unavailable")
    void shouldCalculateSlotsAndMarkOccupiedSlotUnavailable() {
        // Find a future Monday
        LocalDate nextMonday = LocalDate.now().plusWeeks(1);
        while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            nextMonday = nextMonday.plusDays(1);
        }

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(Optional.of(schedule));

        // One booked appointment at 09:30
        Appointment booked = new Appointment(
            null, doctor, nextMonday, LocalTime.of(9, 30), LocalTime.of(10, 0),
            AppointmentStatus.CONFIRMED, "Checkup", null
        );
        when(appointmentRepository.findActiveAppointmentsByDoctorAndDate(1L, nextMonday))
            .thenReturn(List.of(booked));

        List<TimeSlotResponse> slots = doctorDiscoveryService.getAvailableSlots(1L, nextMonday);

        assertThat(slots).hasSize(4);
        // Slot 1: 09:00 - 09:30 -> Available
        assertThat(slots.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(0).isAvailable()).isTrue();

        // Slot 2: 09:30 - 10:00 -> NOT available (already booked)
        assertThat(slots.get(1).getStartTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(slots.get(1).isAvailable()).isFalse();

        // Slot 3: 10:00 - 10:30 -> Available
        assertThat(slots.get(2).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(slots.get(2).isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Slot Calculation: Returns empty list if doctor has no schedule on that day")
    void shouldReturnEmptyListWhenDoctorHasNoScheduleOnDay() {
        LocalDate sunday = LocalDate.now().plusWeeks(1);
        while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            sunday = sunday.plusDays(1);
        }

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepository.findByDoctorIdAndDayOfWeek(1L, DayOfWeek.SUNDAY)).thenReturn(Optional.empty());

        List<TimeSlotResponse> slots = doctorDiscoveryService.getAvailableSlots(1L, sunday);

        assertThat(slots).isEmpty();
    }
}
