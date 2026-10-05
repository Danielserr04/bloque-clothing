package com.bloque.application.port.out;

import com.bloque.domain.model.RefreshToken;

import java.util.Optional;

/** Puerto de SALIDA: refresh tokens guardados (por hash). */
public interface RefreshTokenRepositoryPort {

    void guardar(RefreshToken token);

    Optional<RefreshToken> buscarPorHash(String tokenHash);

    /** Borra el token. Devuelve true solo si existía (así un token no se puede usar dos veces). */
    boolean borrarPorHash(String tokenHash);
}
