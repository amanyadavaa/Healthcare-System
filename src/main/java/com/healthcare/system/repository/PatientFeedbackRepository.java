package com.healthcare.system.repository;

import com.healthcare.system.entity.PatientFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientFeedbackRepository extends JpaRepository<PatientFeedback, Long> {

    Page<PatientFeedback> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);

    Optional<PatientFeedback> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT AVG(f.rating) FROM PatientFeedback f WHERE f.doctor.id = :doctorId")
    Double calculateAverageRatingForDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(f) FROM PatientFeedback f WHERE f.doctor.id = :doctorId")
    Long countFeedbacksForDoctor(@Param("doctorId") Long doctorId);
}
