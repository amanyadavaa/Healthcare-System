package com.healthcare.system.service;

import com.healthcare.system.dto.medical.MedicalRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicalRecordService {

    Page<MedicalRecordResponse> getPatientMedicalRecords(Long userId, Pageable pageable);

    MedicalRecordResponse getMedicalRecordByIdForPatient(Long userId, Long recordId);

    MedicalRecordResponse getMedicalRecordByAppointmentForPatient(Long userId, Long appointmentId);
}
