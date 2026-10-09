package com.healthcare.system.repository;

import com.healthcare.system.entity.Appointment;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Appointment Repo: Verifies slot conflict detection and active appointments query")
    void shouldDetectSlotOccupancyAndQueryActiveAppointments() {
        User docUser = userRepository.save(new User("doc.test@apex.com", "pw", "Gregory", "House", "999", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE));
        Doctor doctor = doctorRepository.save(new Doctor(docUser, "Diagnostic Medicine", "MD", 15, BigDecimal.valueOf(150), "Diagnostics"));

        User patUser = userRepository.save(new User("pat.test@apex.com", "pw", "James", "Wilson", "888", RoleType.ROLE_PATIENT, UserStatus.ACTIVE));
        Patient patient = patientRepository.save(new Patient(patUser, LocalDate.of(1985, 5, 20), "Male", "O+", "555-1234", "123 Street"));

        LocalDate today = LocalDate.now().plusDays(2);
        LocalTime slotTime = LocalTime.of(10, 0);

        Appointment appt = new Appointment(
            patient, doctor, today, slotTime, slotTime.plusMinutes(30),
            AppointmentStatus.CONFIRMED, "Regular checkup", "Initial visit"
        );
        appointmentRepository.save(appt);

        // Verify slot check
        boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(doctor.getId(), today, slotTime);
        assertThat(exists).isTrue();

        boolean freeSlot = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(doctor.getId(), today, LocalTime.of(11, 0));
        assertThat(freeSlot).isFalse();

        // Verify active query
        List<Appointment> active = appointmentRepository.findActiveAppointmentsByDoctorAndDate(doctor.getId(), today);
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getPatient().getUser().getEmail()).isEqualTo("pat.test@apex.com");
    }
}
