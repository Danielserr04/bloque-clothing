package com.bloque.infrastructure.persistence;

import com.bloque.application.port.out.UsuarioRepositoryPort;
import com.bloque.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de SALIDA: implementa el puerto de usuarios usando JPA. */
@Component
class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repo;

    UsuarioPersistenceAdapter(UsuarioJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repo.findByEmail(email).map(UsuarioEntity::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repo.findById(id).map(UsuarioEntity::aDominio);
    }

    @Override
    public boolean existeEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return repo.save(UsuarioEntity.desdeDominio(usuario)).aDominio();
    }
}
