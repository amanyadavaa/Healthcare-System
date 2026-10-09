package com.healthcare.system.service;

import com.healthcare.system.dto.auth.AuthResponse;
import com.healthcare.system.dto.auth.LoginRequest;
import com.healthcare.system.dto.auth.RegisterRequest;
import com.healthcare.system.dto.auth.UserProfileResponse;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.Patient;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.exception.ConflictException;
import com.healthcare.system.exception.ResourceNotFoundException;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.repository.PatientRepository;
import com.healthcare.system.repository.UserRepository;
import com.healthcare.system.security.JwtTokenProvider;
import com.healthcare.system.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
        UserRepository userRepository,
        PatientRepository patientRepository,
        DoctorRepository doctorRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail().trim().toLowerCase(),
                loginRequest.getPassword()
            )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(
            jwt,
            userPrincipal.getId(),
            userPrincipal.getUsername(),
            userPrincipal.getFirstName(),
            userPrincipal.getLastName(),
            userPrincipal.getRole()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        String normalizedEmail = registerRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("An account with email " + normalizedEmail + " already exists.");
        }

        RoleType role = registerRequest.getRole() != null ? registerRequest.getRole() : RoleType.ROLE_PATIENT;

        User user = new User(
            normalizedEmail,
            passwordEncoder.encode(registerRequest.getPassword()),
            registerRequest.getFirstName().trim(),
            registerRequest.getLastName().trim(),
            registerRequest.getPhone(),
            role,
            UserStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        if (role == RoleType.ROLE_PATIENT) {
            Patient patient = new Patient(
                savedUser,
                null,
                registerRequest.getGender(),
                registerRequest.getBloodGroup(),
                registerRequest.getEmergencyContact(),
                registerRequest.getAddress()
            );
            patientRepository.save(patient);
        } else if (role == RoleType.ROLE_DOCTOR) {
            Doctor doctor = new Doctor(
                savedUser,
                "General Medicine",
                "MBBS",
                0,
                BigDecimal.ZERO,
                "General"
            );
            doctorRepository.save(doctor);
        }

        String jwt = tokenProvider.generateTokenFromUsername(
            savedUser.getEmail(),
            savedUser.getId(),
            savedUser.getRole().name()
        );

        return new AuthResponse(
            jwt,
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getRole(),
            user.getStatus()
        );
    }
}
