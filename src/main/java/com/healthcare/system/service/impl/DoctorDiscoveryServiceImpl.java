package com.healthcare.system.service.impl;

import com.healthcare.system.dto.doctor.DoctorResponse;
import com.healthcare.system.dto.doctor.TimeSlotResponse;
import com.healthcare.system.entity.Appointment;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.DoctorSchedule;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.AppointmentRepository;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.repository.DoctorScheduleRepository;
import com.healthcare.system.service.DoctorDiscoveryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorDiscoveryServiceImpl implements DoctorDiscoveryService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorDiscoveryServiceImpl(
        DoctorRepository doctorRepository,
        DoctorScheduleRepository scheduleRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> searchDoctors(String query, String specialization, String department, Pageable pageable) {
        Specification<Doctor> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate userFirstNameMatch = cb.like(cb.lower(root.get("user").get("firstName")), pattern);
                Predicate userLastNameMatch = cb.like(cb.lower(root.get("user").get("lastName")), pattern);
                Predicate specMatch = cb.like(cb.lower(root.get("specialization")), pattern);
                Predicate deptMatch = cb.like(cb.lower(root.get("department")), pattern);
                predicates.add(cb.or(userFirstNameMatch, userLastNameMatch, specMatch, deptMatch));
            }

            if (specialization != null && !specialization.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("specialization")), specialization.trim().toLowerCase()));
            }

            if (department != null && !department.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return doctorRepository.findAll(spec, pageable).map(this::mapToDoctorResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        return mapToDoctorResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek());
        if (scheduleOpt.isEmpty() || !scheduleOpt.get().getIsAvailable()) {
            return Collections.emptyList();
        }

        DoctorSchedule schedule = scheduleOpt.get();
        LocalTime startTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        int slotDuration = schedule.getSlotDurationMinutes() != null && schedule.getSlotDurationMinutes() > 0
            ? schedule.getSlotDurationMinutes() : 30;

        List<Appointment> bookedAppointments = appointmentRepository.findActiveAppointmentsByDoctorAndDate(doctorId, date);
        Set<LocalTime> bookedStartTimes = bookedAppointments.stream()
            .map(Appointment::getStartTime)
            .collect(Collectors.toSet());

        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime currentSlotStart = startTime;

        while (currentSlotStart.plusMinutes(slotDuration).isBefore(endTime)
                || currentSlotStart.plusMinutes(slotDuration).equals(endTime)) {
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(slotDuration);
            boolean isAvailable = !bookedStartTimes.contains(currentSlotStart);

            // If the date is today, slot must also be in future
            if (date.isEqual(LocalDate.now()) && currentSlotStart.isBefore(LocalTime.now())) {
                isAvailable = false;
            }

            slots.add(new TimeSlotResponse(currentSlotStart, currentSlotEnd, isAvailable));
            currentSlotStart = currentSlotEnd;
        }

        return slots;
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.getId(),
            doctor.getUser().getId(),
            doctor.getUser().getFullName(),
            doctor.getUser().getEmail(),
            doctor.getUser().getPhone(),
            doctor.getSpecialization(),
            doctor.getQualification(),
            doctor.getExperienceYears(),
            doctor.getConsultationFee(),
            doctor.getDepartment(),
            doctor.getAverageRating(),
            doctor.getTotalRatings()
        );
    }
}
