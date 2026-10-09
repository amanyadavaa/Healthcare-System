package com.healthcare.system.service;

import com.healthcare.system.dto.doctor.DoctorResponse;
import com.healthcare.system.entity.Doctor;
import com.healthcare.system.entity.User;
import com.healthcare.system.entity.enums.RoleType;
import com.healthcare.system.entity.enums.UserStatus;
import com.healthcare.system.repository.DoctorRepository;
import com.healthcare.system.service.impl.DoctorDiscoveryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorSearchFilterTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorDiscoveryServiceImpl doctorDiscoveryService;

    @Test
    @DisplayName("Doctor Search: Filters doctors with pagination specification")
    void shouldSearchAndReturnPaginatedDoctors() {
        User user = new User("dr.cardio@apexcare.health", "pw", "Gregory", "House", "123", RoleType.ROLE_DOCTOR, UserStatus.ACTIVE);
        user.setId(5L);
        Doctor doctor = new Doctor(user, "Cardiology", "MD, FACC", 15, BigDecimal.valueOf(200), "Cardiology");
        doctor.setId(2L);

        Page<Doctor> doctorPage = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(doctorPage);

        Page<DoctorResponse> result = doctorDiscoveryService.searchDoctors("Cardiology", "Cardiology", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        DoctorResponse first = result.getContent().get(0);
        assertThat(first.getFullName()).isEqualTo("Gregory House");
        assertThat(first.getSpecialization()).isEqualTo("Cardiology");
    }
}
