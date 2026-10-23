package com.fulls_bank.Fulls_Bank.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void jwtDecoder_withoutSecret_shouldFailFast() {
        assertThrows(IllegalStateException.class, () -> config.jwtDecoder(new JwtProperties(null, 60)));
    }

    @Test
    void jwtDecoder_withShortSecret_shouldFailFast() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> config.jwtDecoder(new JwtProperties("short", 60)));

        assertTrue(error.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void jwtEncoder_withUnresolvedPlaceholder_shouldFailFast() {
        assertThrows(IllegalStateException.class, () -> config.jwtEncoder(new JwtProperties("${JWT_SECRET}", 60)));
    }

    @Test
    void jwtBeans_withValidSecret_shouldBeCreated() {
        JwtProperties properties = new JwtProperties("a-secret-with-at-least-32-bytes-long!", 60);

        assertDoesNotThrow(() -> config.jwtDecoder(properties));
        assertDoesNotThrow(() -> config.jwtEncoder(properties));
    }
}
