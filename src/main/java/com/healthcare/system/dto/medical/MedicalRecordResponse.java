package com.healthcare.system.dto.medical;

import java.time.LocalDateTime;

public class MedicalRecordResponse {

    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String diagnosis;
    private String prescription;
    private String treatmentNotes;
    private String vitalsBp;
    private String vitalsPulse;
    private String vitalsTemp;
    private LocalDateTime createdAt;

    public MedicalRecordResponse() {}

    public MedicalRecordResponse(Long id, Long appointmentId, Long patientId, String patientName,
                                 Long doctorId, String doctorName, String doctorSpecialization,
                                 String diagnosis, String prescription, String treatmentNotes,
                                 String vitalsBp, String vitalsPulse, String vitalsTemp,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.treatmentNotes = treatmentNotes;
        this.vitalsBp = vitalsBp;
        this.vitalsPulse = vitalsPulse;
        this.vitalsTemp = vitalsTemp;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }

    public String getVitalsBp() {
        return vitalsBp;
    }

    public void setVitalsBp(String vitalsBp) {
        this.vitalsBp = vitalsBp;
    }

    public String getVitalsPulse() {
        return vitalsPulse;
    }

    public void setVitalsPulse(String vitalsPulse) {
        this.vitalsPulse = vitalsPulse;
    }

    public String getVitalsTemp() {
        return vitalsTemp;
    }

    public void setVitalsTemp(String vitalsTemp) {
        this.vitalsTemp = vitalsTemp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
