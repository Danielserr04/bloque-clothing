package com.bloque.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Repositorios de Spring Data (los usan los adaptadores, no la capa de aplicación). */
interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}

interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {
}

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {
    /** Devuelve cuántas filas borró (0 o 1). */
    @Modifying
    @Query("delete from RefreshTokenEntity t where t.tokenHash = :tokenHash")
    int borrarPorHash(String tokenHash);
}
