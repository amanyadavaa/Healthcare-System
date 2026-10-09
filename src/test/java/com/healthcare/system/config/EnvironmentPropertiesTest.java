package com.healthcare.system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnvironmentPropertiesTest {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.clinic.name}")
    private String clinicName;

    @Test
    @DisplayName("Environment Properties: Verifies test profile correctly resolves application, JWT, and clinic configurations")
    void shouldLoadExpectedPropertiesForTestProfile() {
        assertThat(applicationName).isEqualTo("healthcare-system");
        assertThat(jwtExpirationMs).isEqualTo(3600000L);
        assertThat(clinicName).isEqualTo("ApexCare Test Clinic");
    }
}
