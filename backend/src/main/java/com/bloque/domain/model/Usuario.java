package com.bloque.domain.model;

import java.util.Objects;

/**
 * Usuario de la tienda (capa de dominio: Java puro, sin Spring ni JPA).
 * Nunca guarda la contraseña en claro, solo su hash.
 */
public record Usuario(Long id, String email, String passwordHash, String nombre, Rol rol) {

    public Usuario {
        Objects.requireNonNull(email, "email obligatorio");
        Objects.requireNonNull(passwordHash, "passwordHash obligatorio");
        Objects.requireNonNull(rol, "rol obligatorio");
        email = email.trim().toLowerCase();
    }

    /** Crea un usuario nuevo (sin id todavía) con rol USER. */
    public static Usuario nuevoCliente(String email, String passwordHash, String nombre) {
        return new Usuario(null, email, passwordHash, nombre, Rol.USER);
    }
}
