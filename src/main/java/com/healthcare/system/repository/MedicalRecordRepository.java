package com.healthcare.system.repository;

import com.healthcare.system.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}
