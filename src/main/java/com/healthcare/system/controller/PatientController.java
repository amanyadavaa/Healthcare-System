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

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
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
