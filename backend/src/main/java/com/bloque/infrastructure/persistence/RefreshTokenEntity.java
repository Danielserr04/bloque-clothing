package com.bloque.infrastructure.persistence;

import com.bloque.domain.model.RefreshToken;
import jakarta.persistence.*;

import java.time.Instant;

/** Tabla "refresh_tokens". La clave es el hash SHA-256 del token, nunca el token en claro. */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    protected RefreshTokenEntity() {
    }

    static RefreshTokenEntity desdeDominio(RefreshToken t) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.tokenHash = t.tokenHash();
        e.usuarioId = t.usuarioId();
        e.expiraEn = t.expiraEn();
        return e;
    }

    RefreshToken aDominio() {
        return new RefreshToken(tokenHash, usuarioId, expiraEn);
    }
}
