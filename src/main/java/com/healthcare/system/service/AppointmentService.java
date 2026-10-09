package com.healthcare.system.service;

import com.healthcare.system.dto.appointment.AppointmentBookingRequest;
import com.healthcare.system.dto.appointment.AppointmentResponse;
import com.healthcare.system.entity.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentService {

    AppointmentResponse bookAppointment(Long userId, AppointmentBookingRequest request);

    Page<AppointmentResponse> getPatientAppointments(Long userId, AppointmentStatus status, String timeframe, Pageable pageable);

    AppointmentResponse cancelAppointmentByPatient(Long userId, Long appointmentId, String reason);

    AppointmentResponse rescheduleAppointmentByPatient(Long userId, Long appointmentId, LocalDate newDate, LocalTime newStartTime);
}
