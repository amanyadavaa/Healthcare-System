package com.healthcare.system.repository;

import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User Repo: Successfully saves and retrieves user by case-insensitive email")
    void shouldSaveAndFindUserByEmailIgnoreCase() {
        User user = new User("Doctor.Smith@apexcare.health", "encodedPwd", "John", "Smith", "1234567890", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase("doctor.smith@apexcare.health");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getRole()).isEqualTo(RoleType.ROLE_DOCTOR);
    }

    @Test
    @DisplayName("User Repo: Filters users by role with pagination")
    void shouldFindAllUsersByRoleWithPagination() {
        userRepository.save(new User("p1@apexcare.health", "p", "Pat", "One", "111", RoleType.ROLE_PATIENT, UserStatus.ACTIVE));
        userRepository.save(new User("p2@apexcare.health", "p", "Pat", "Two", "222", RoleType.ROLE_PATIENT, UserStatus.ACTIVE));
        userRepository.save(new User("d1@apexcare.health", "p", "Doc", "One", "333", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE));

        Page<User> patients = userRepository.findAllByRole(RoleType.ROLE_PATIENT, PageRequest.of(0, 10));

        assertThat(patients.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(userRepository.countByRole(RoleType.ROLE_PATIENT)).isGreaterThanOrEqualTo(2);
    }
}
