package com.healthcare.system.service;

import com.healthcare.system.dto.patient.PasswordChangeRequest;
import com.healthcare.system.dto.patient.PatientProfileResponse;
import com.healthcare.system.dto.patient.PatientProfileUpdateRequest;

public interface PatientService {

    PatientProfileResponse getPatientProfile(Long userId);

    PatientProfileResponse updatePatientProfile(Long userId, PatientProfileUpdateRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);
}
