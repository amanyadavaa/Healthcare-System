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

    public PatientController(PatientService patientService, com.healthcare.system.service.DoctorDiscoveryService doctorDiscoveryService) {
        this.patientService = patientService;
        this.doctorDiscoveryService = doctorDiscoveryService;
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
