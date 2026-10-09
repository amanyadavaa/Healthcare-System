package com.healthcare.system.service.impl;

import com.healthcare.system.dto.patient.PasswordChangeRequest;
import com.healthcare.system.dto.patient.PatientProfileResponse;
import com.healthcare.system.dto.patient.PatientProfileUpdateRequest;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.exception.BadRequestException;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.repository.UserRepository;
import com.healthcare.system.service.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientServiceImpl(UserRepository userRepository, PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Patient patient = patientRepository.findByUserId(userId)
            .orElseGet(() -> {
                Patient newPatient = new Patient();
                newPatient.setUser(user);
                return patientRepository.save(newPatient);
            });

        return mapToProfileResponse(user, patient);
    }

    @Override
    @Transactional
    public PatientProfileResponse updatePatientProfile(Long userId, PatientProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        userRepository.save(user);

        Patient patient = patientRepository.findByUserId(userId)
            .orElseGet(() -> {
                Patient newPatient = new Patient();
                newPatient.setUser(user);
                return newPatient;
            });

        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setAddress(request.getAddress());
        Patient savedPatient = patientRepository.save(patient);

        return mapToProfileResponse(user, savedPatient);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password provided does not match our records.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be identical to the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private PatientProfileResponse mapToProfileResponse(User user, Patient patient) {
        return new PatientProfileResponse(
            patient.getId(),
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            patient.getDateOfBirth(),
            patient.getGender(),
            patient.getBloodGroup(),
            patient.getEmergencyContact(),
            patient.getAddress(),
            user.getCreatedAt()
        );
    }
}
