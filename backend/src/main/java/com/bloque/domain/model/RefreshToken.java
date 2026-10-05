package com.bloque.domain.model;

import java.time.Instant;

/**
 * Refresh token guardado en BD. Solo guardamos el HASH del token,
 * así si alguien roba la BD no puede usar los tokens.
 */
public record RefreshToken(String tokenHash, Long usuarioId, Instant expiraEn) {

    public boolean caducado(Instant ahora) {
        return ahora.isAfter(expiraEn);
    }
}
