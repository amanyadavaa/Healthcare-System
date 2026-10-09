package com.healthcare.system.service.impl;

import com.healthcare.system.dto.appointment.AppointmentBookingRequest;
import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.Appointment;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.DoctorSchedule;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.enums.AppointmentStatus;
import com.healthcare.system.exception.BadRequestException;
import com.healthcare.system.exception.ConflictException;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.AppointmentRepository;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.repository.DoctorScheduleRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.service.AppointmentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public AppointmentServiceImpl(
        AppointmentRepository appointmentRepository,
        PatientRepository patientRepository,
        DoctorRepository doctorRepository,
        DoctorScheduleRepository scheduleRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(Long userId, AppointmentBookingRequest request) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        LocalDate date = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();

        // 1. Validate date & time
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past.");
        }
        if (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new BadRequestException("Appointment time cannot be in the past.");
        }

        // 2. Validate doctor schedule
        DoctorSchedule schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), date.getDayOfWeek())
            .orElseThrow(() -> new BadRequestException("Doctor is not scheduled to consult on " + date.getDayOfWeek()));

        if (!schedule.getIsAvailable()) {
            throw new BadRequestException("Doctor is not available on " + date.getDayOfWeek());
        }

        int slotDuration = (schedule.getSlotDurationMinutes() != null && schedule.getSlotDurationMinutes() > 0)
            ? schedule.getSlotDurationMinutes() : 30;

        if (startTime.isBefore(schedule.getStartTime()) || startTime.plusMinutes(slotDuration).isAfter(schedule.getEndTime())) {
            throw new BadRequestException("Selected appointment slot falls outside doctor's working schedule.");
        }

        // 3. Prevent double booking
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(doctor.getId(), date, startTime)) {
            throw new ConflictException("The selected time slot is already booked. Please choose an alternative slot.");
        }

        LocalTime endTime = startTime.plusMinutes(slotDuration);

        Appointment appointment = new Appointment(
            patient,
            doctor,
            date,
            startTime,
            endTime,
            AppointmentStatus.CONFIRMED,
            request.getReason().trim(),
            request.getNotes()
        );

        Appointment saved = appointmentRepository.save(appointment);
        return mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getPatientAppointments(Long userId, AppointmentStatus status, String timeframe, Pageable pageable) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        Specification<Appointment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("patient").get("id"), patient.getId()));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if ("UPCOMING".equalsIgnoreCase(timeframe)) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("appointmentDate"), LocalDate.now()));
            } else if ("PAST".equalsIgnoreCase(timeframe)) {
                predicates.add(cb.lessThan(root.get("appointmentDate"), LocalDate.now()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return appointmentRepository.findAll(spec, pageable).map(this::mapToAppointmentResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointmentByPatient(Long userId, Long appointmentId, String reason) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("You do not have authorization to cancel this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Completed appointments cannot be cancelled.");
        }

        LocalDateTime apptDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime());
        if (LocalDateTime.now().isAfter(apptDateTime)) {
            throw new BadRequestException("Cannot cancel an appointment that has already elapsed.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String updatedNotes = (appointment.getNotes() != null ? appointment.getNotes() + " | Cancellation Reason: " : "Cancellation Reason: ") + reason.trim();
            appointment.setNotes(updatedNotes);
        }

        Appointment saved = appointmentRepository.save(appointment);
        return mapToAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointmentByPatient(Long userId, Long appointmentId, LocalDate newDate, LocalTime newStartTime) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("You do not have authorization to reschedule this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot reschedule an appointment with status: " + appointment.getStatus());
        }

        if (newDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Reschedule date cannot be in the past.");
        }
        if (newDate.isEqual(LocalDate.now()) && newStartTime.isBefore(LocalTime.now())) {
            throw new BadRequestException("Reschedule time cannot be in the past.");
        }

        DoctorSchedule schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(appointment.getDoctor().getId(), newDate.getDayOfWeek())
            .orElseThrow(() -> new BadRequestException("Doctor is not scheduled to work on " + newDate.getDayOfWeek()));

        int slotDuration = (schedule.getSlotDurationMinutes() != null && schedule.getSlotDurationMinutes() > 0)
            ? schedule.getSlotDurationMinutes() : 30;

        if (newStartTime.isBefore(schedule.getStartTime()) || newStartTime.plusMinutes(slotDuration).isAfter(schedule.getEndTime())) {
            throw new BadRequestException("Selected slot falls outside doctor's schedule.");
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(appointment.getDoctor().getId(), newDate, newStartTime)) {
            throw new ConflictException("The selected slot is already occupied. Please pick another slot.");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newStartTime.plusMinutes(slotDuration));
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        return mapToAppointmentResponse(saved);
    }

    private AppointmentResponse mapToAppointmentResponse(Appointment appt) {
        return new AppointmentResponse(
            appt.getId(),
            appt.getPatient().getId(),
            appt.getPatient().getUser().getFullName(),
            appt.getDoctor().getId(),
            appt.getDoctor().getUser().getFullName(),
            appt.getDoctor().getSpecialization(),
            appt.getAppointmentDate(),
            appt.getStartTime(),
            appt.getEndTime(),
            appt.getStatus(),
            appt.getReason(),
            appt.getNotes(),
            appt.getCreatedAt()
        );
    }
}
