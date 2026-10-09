package com.healthcare.system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs, 7200000);
    }

    @Test
    @DisplayName("JWT: Generates and parses valid claims from token")
    void shouldGenerateAndParseValidToken() {
        String token = tokenProvider.generateTokenFromUsername("alice@apexcare.health", 42L, "ROLE_PATIENT");

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromJwt(token)).isEqualTo("alice@apexcare.health");
        assertThat(tokenProvider.getUserIdFromJwt(token)).isEqualTo(42L);
        assertThat(tokenProvider.getRoleFromJwt(token)).isEqualTo("ROLE_PATIENT");
    }

    @Test
    @DisplayName("JWT: Rejects tampered or malformed tokens")
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.invalidPayload.invalidSignature";
        assertThat(tokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("JWT: Rejects expired tokens")
    void shouldRejectExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, -1000, 1000);
        String expiredToken = shortLivedProvider.generateTokenFromUsername("expired@apexcare.health", 1L, "ROLE_ADMIN");

        assertThat(shortLivedProvider.validateToken(expiredToken)).isFalse();
    }
}
