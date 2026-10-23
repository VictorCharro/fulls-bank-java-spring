package com.fulls_bank.Fulls_Bank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do JWT: {@code app.jwt.secret} (variável JWT_SECRET, mínimo de 32 bytes para HS256)
 * e {@code app.jwt.expiration-minutes}.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
