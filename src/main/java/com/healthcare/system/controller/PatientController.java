package com.healthcare.system.controller;

import com.healthcare.system.dto.common.ApiResponse;
import com.healthcare.system.dto.patient.PasswordChangeRequest;
import com.healthcare.system.dto.patient.PatientProfileResponse;
import com.healthcare.system.dto.patient.PatientProfileUpdateRequest;
import com.healthcare.system.security.UserPrincipal;
import com.healthcare.system.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
@Tag(name = "Patient Module", description = "Endpoints for patient dashboard, profile management, and clinical features")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;
    private final com.healthcare.system.service.DoctorDiscoveryService doctorDiscoveryService;
    private final com.healthcare.system.service.AppointmentService appointmentService;

    public PatientController(
        PatientService patientService,
        com.healthcare.system.service.DoctorDiscoveryService doctorDiscoveryService,
        com.healthcare.system.service.AppointmentService appointmentService
    ) {
        this.patientService = patientService;
        this.doctorDiscoveryService = doctorDiscoveryService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments")
    @Operation(summary = "Book a new doctor appointment slot")
    public ResponseEntity<ApiResponse<com.healthcare.system.dto.appointment.AppointmentResponse>> bookAppointment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody com.healthcare.system.dto.appointment.AppointmentBookingRequest request) {
        com.healthcare.system.dto.appointment.AppointmentResponse response = 
            appointmentService.bookAppointment(userPrincipal.getId(), request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
            .body(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get paginated appointment history with optional status and timeframe filters")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.healthcare.system.dto.appointment.AppointmentResponse>>> getAppointments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) com.healthcare.system.entity.enums.AppointmentStatus status,
            @RequestParam(required = false) String timeframe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<com.healthcare.system.dto.appointment.AppointmentResponse> appointments = 
            appointmentService.getPatientAppointments(userPrincipal.getId(), status, timeframe, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved successfully", appointments));
    }

    @PutMapping("/appointments/{id}/cancel")
    @Operation(summary = "Cancel an existing upcoming appointment")
    public ResponseEntity<ApiResponse<com.healthcare.system.dto.appointment.AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody(required = false) com.healthcare.system.dto.appointment.AppointmentCancelRequest request) {
        String reason = request != null ? request.getReason() : null;
        com.healthcare.system.dto.appointment.AppointmentResponse response = 
            appointmentService.cancelAppointmentByPatient(userPrincipal.getId(), id, reason);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", response));
    }

    @PutMapping("/appointments/{id}/reschedule")
    @Operation(summary = "Reschedule an appointment to a new date and time slot")
    public ResponseEntity<ApiResponse<com.healthcare.system.dto.appointment.AppointmentResponse>> rescheduleAppointment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody com.healthcare.system.dto.appointment.AppointmentRescheduleRequest request) {
        com.healthcare.system.dto.appointment.AppointmentResponse response = 
            appointmentService.rescheduleAppointmentByPatient(userPrincipal.getId(), id, request.getNewDate(), request.getNewStartTime());
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled successfully", response));
    }

    @GetMapping("/doctors")
    @Operation(summary = "Search and filter doctors by name, specialization, or department")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.healthcare.system.dto.doctor.DoctorResponse>>> getDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<com.healthcare.system.dto.doctor.DoctorResponse> doctors = 
            doctorDiscoveryService.searchDoctors(search, specialization, department, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctors));
    }

    @GetMapping("/doctors/{id}")
    @Operation(summary = "Get detailed profile of a specific doctor")
    public ResponseEntity<ApiResponse<com.healthcare.system.dto.doctor.DoctorResponse>> getDoctorById(@PathVariable Long id) {
        com.healthcare.system.dto.doctor.DoctorResponse doctor = doctorDiscoveryService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor details retrieved successfully", doctor));
    }

    @GetMapping("/doctors/{id}/available-slots")
    @Operation(summary = "Retrieve available appointment slots for a doctor on a specific date")
    public ResponseEntity<ApiResponse<java.util.List<com.healthcare.system.dto.doctor.TimeSlotResponse>>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        java.util.List<com.healthcare.system.dto.doctor.TimeSlotResponse> slots = doctorDiscoveryService.getAvailableSlots(id, date);
        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved successfully", slots));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current patient profile details")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        PatientProfileResponse profile = patientService.getPatientProfile(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Patient profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update patient profile personal and medical demographic details")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PatientProfileUpdateRequest request) {
        PatientProfileResponse updated = patientService.updatePatientProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Patient profile updated successfully", updated));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current patient account password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PasswordChangeRequest request) {
        patientService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
