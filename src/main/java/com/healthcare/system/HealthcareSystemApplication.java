package com.healthcare.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HealthcareSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcareSystemApplication.class, args);
    }
}
