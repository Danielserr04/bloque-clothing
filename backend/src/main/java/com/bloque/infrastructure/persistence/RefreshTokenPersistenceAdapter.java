package com.bloque.infrastructure.persistence;

import com.bloque.application.port.out.RefreshTokenRepositoryPort;
import com.bloque.domain.model.RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptador de SALIDA: refresh tokens en BD. */
@Component
class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repo;

    RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void guardar(RefreshToken token) {
        repo.save(RefreshTokenEntity.desdeDominio(token));
    }

    @Override
    public Optional<RefreshToken> buscarPorHash(String tokenHash) {
        return repo.findById(tokenHash).map(RefreshTokenEntity::aDominio);
    }

    @Override
    @Transactional
    public boolean borrarPorHash(String tokenHash) {
        return repo.borrarPorHash(tokenHash) > 0;
    }
}
