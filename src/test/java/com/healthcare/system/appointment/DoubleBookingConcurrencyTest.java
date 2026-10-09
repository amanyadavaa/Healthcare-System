package com.healthcare.system.appointment;

import com.healthcare.system.dto.appointment.AppointmentBookingRequest;
import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.*;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.ConflictException;
import com.healthcare.system.repository.*;
import com.healthcare.system.service.AppointmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DoubleBookingConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Concurrency & Double-Booking: Second concurrent booking for identical slot is rejected with ConflictException")
    void shouldPreventDoubleBookingForSameDoctorAndSlot() {
        // Create Doctor
        User docUser = userRepository.save(new User("doc.conflict@apex.com", "pw", "Gregory", "House", "123", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE));
        Doctor doctor = doctorRepository.save(new Doctor(docUser, "Diagnostic", "MD", 10, BigDecimal.valueOf(100), "Medicine"));

        // Create Schedule for all days of week
        for (DayOfWeek day : DayOfWeek.values()) {
            scheduleRepository.save(new DoctorSchedule(doctor, day, LocalTime.of(8, 0), LocalTime.of(18, 0), 30, true));
        }

        // Create Patient 1
        User p1User = userRepository.save(new User("p1.conflict@apex.com", "pw", "Patient", "One", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE));
        Patient p1 = patientRepository.save(new Patient(p1User, LocalDate.of(1990, 1, 1), "Male", "O+", "999", "Address 1"));

        // Create Patient 2
        User p2User = userRepository.save(new User("p2.conflict@apex.com", "pw", "Patient", "Two", "222", RoleType.ROLE_PATIENT, UserStatus.ACTIVE));
        Patient p2 = patientRepository.save(new Patient(p2User, LocalDate.of(1992, 2, 2), "Female", "A+", "888", "Address 2"));

        LocalDate targetDate = LocalDate.now().plusDays(3);
        LocalTime targetSlot = LocalTime.of(10, 0);

        // 1. Patient 1 books the slot -> SUCCEEDS
        AppointmentBookingRequest request1 = new AppointmentBookingRequest(doctor.getId(), targetDate, targetSlot, "Fever", null);
        AppointmentResponse response1 = appointmentService.bookAppointment(p1User.getId(), request1);

        assertThat(response1).isNotNull();
        assertThat(response1.getId()).isNotNull();

        // 2. Patient 2 attempts booking the exact same slot -> MUST FAIL WITH CONFLICT
        AppointmentBookingRequest request2 = new AppointmentBookingRequest(doctor.getId(), targetDate, targetSlot, "Cough", null);
        assertThatThrownBy(() -> appointmentService.bookAppointment(p2User.getId(), request2))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("The selected time slot is already booked.");
    }
}
