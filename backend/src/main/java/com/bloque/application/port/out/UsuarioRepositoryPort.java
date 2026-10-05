package com.bloque.application.port.out;

import com.bloque.domain.model.Usuario;

import java.util.Optional;

/** Puerto de SALIDA: dónde se guardan los usuarios (la app no sabe si es H2, Postgres...). */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long id);

    boolean existeEmail(String email);

    Usuario guardar(Usuario usuario);
}
