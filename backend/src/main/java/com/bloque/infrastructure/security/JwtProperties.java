package com.bloque.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Configuración de JWT (en application.yml, bloque.jwt.*).
 * El secreto NUNCA va en el código: en producción viene de la variable de entorno JWT_SECRET.
 */
@ConfigurationProperties(prefix = "bloque.jwt")
public record JwtProperties(String secret, String issuer, long accessTokenMinutos, long refreshTokenDias) {

    public JwtProperties {
        // HS256 necesita una clave de al menos 256 bits (32 bytes). Si no, la app no arranca.
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("bloque.jwt.secret (JWT_SECRET) debe tener al menos 32 caracteres");
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "bloque-api";
        }
        if (accessTokenMinutos <= 0) {
            accessTokenMinutos = 15;
        }
        if (refreshTokenDias <= 0) {
            refreshTokenDias = 7;
        }
    }
}
