package com.healthcare.system.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SchemaIntegrityTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupSchema() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/migration/V1__init_schema.sql"));
        populator.execute(dataSource);
    }

    @Test
    @DisplayName("Schema Integrity: Verifies all core healthcare tables exist and are queryable")
    void shouldVerifyAllHealthcareTablesExist() {
        String[] tables = {
            "users", "patients", "doctors", "doctor_schedules",
            "appointments", "medical_records", "patient_feedbacks", "system_settings"
        };

        for (String table : tables) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            assertThat(count).isNotNull().isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("Unique Constraint: Enforces unique email constraint on users table")
    void shouldEnforceUniqueEmailConstraint() {
        jdbcTemplate.update(
            "INSERT INTO users (email, password, first_name, last_name, role, status) VALUES (?, ?, ?, ?, ?, ?)",
            "unique.test@example.com", "hash123", "Jane", "Doe", "ROLE_PATIENT", "ACTIVE"
        );

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO users (email, password, first_name, last_name, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                "unique.test@example.com", "hash456", "Another", "Doe", "ROLE_PATIENT", "ACTIVE"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
