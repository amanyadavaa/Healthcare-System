package com.healthcare.system.service.impl;

import com.healthcare.system.dto.medical.MedicalRecordResponse;
import com.healthcare.system.entity.MedicalRecord;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.MedicalRecordRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.service.MedicalRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    public MedicalRecordServiceImpl(MedicalRecordRepository medicalRecordRepository, PatientRepository patientRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getPatientMedicalRecords(Long userId, Pageable pageable) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        return medicalRecordRepository.findByPatientId(patient.getId(), pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordByIdForPatient(Long userId, Long recordId) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        MedicalRecord record = medicalRecordRepository.findById(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", recordId));

        if (!record.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("Access denied: You are not authorized to view this medical record.");
        }

        return mapToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordByAppointmentForPatient(Long userId, Long appointmentId) {
        Patient patient = patientRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for current user"));

        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Medical record not found for appointment id: " + appointmentId));

        if (!record.getPatient().getId().equals(patient.getId())) {
            throw new AccessDeniedException("Access denied: You are not authorized to view this medical record.");
        }

        return mapToResponse(record);
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return new MedicalRecordResponse(
            record.getId(),
            record.getAppointment().getId(),
            record.getPatient().getId(),
            record.getPatient().getUser().getFullName(),
            record.getDoctor().getId(),
            record.getDoctor().getUser().getFullName(),
            record.getDoctor().getSpecialization(),
            record.getDiagnosis(),
            record.getPrescription(),
            record.getTreatmentNotes(),
            record.getVitalsBp(),
            record.getVitalsPulse(),
            record.getVitalsTemp(),
            record.getCreatedAt()
        );
    }
}
