package com.bloque.domain.model;

/** Par de tokens que devolvemos al hacer login o refresh. */
public record Tokens(String accessToken, String refreshToken, long expiraEnSegundos) {
}
