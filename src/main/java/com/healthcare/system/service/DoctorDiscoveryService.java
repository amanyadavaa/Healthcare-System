package com.healthcare.system.service;

import com.healthcare.system.dto.doctor.DoctorResponse;
import com.healthcare.system.dto.doctor.TimeSlotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DoctorDiscoveryService {

    Page<DoctorResponse> searchDoctors(String query, String specialization, String department, Pageable pageable);

    DoctorResponse getDoctorById(Long doctorId);

    List<TimeSlotResponse> getAvailableSlots(Long doctorId, LocalDate date);
}
