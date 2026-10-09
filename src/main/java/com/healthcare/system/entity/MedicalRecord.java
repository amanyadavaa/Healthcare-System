package com.healthcare.system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_records")
public class MedicalRecord extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 1000)
    private String diagnosis;

    @Column(length = 2000)
    private String prescription;

    @Column(name = "treatment_notes", length = 2000)
    private String treatmentNotes;

    @Column(name = "vitals_bp", length = 20)
    private String vitalsBp;

    @Column(name = "vitals_pulse", length = 20)
    private String vitalsPulse;

    @Column(name = "vitals_temp", length = 20)
    private String vitalsTemp;

    public MedicalRecord() {}

    public MedicalRecord(Appointment appointment, Patient patient, Doctor doctor, String diagnosis, String prescription, String treatmentNotes, String vitalsBp, String vitalsPulse, String vitalsTemp) {
        this.appointment = appointment;
        this.patient = patient;
        this.doctor = doctor;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.treatmentNotes = treatmentNotes;
        this.vitalsBp = vitalsBp;
        this.vitalsPulse = vitalsPulse;
        this.vitalsTemp = vitalsTemp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
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
}
