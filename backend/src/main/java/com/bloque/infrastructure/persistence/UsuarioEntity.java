package com.bloque.infrastructure.persistence;

import com.bloque.domain.model.Rol;
import com.bloque.domain.model.Usuario;
import jakarta.persistence.*;

/** Tabla "usuarios". Las entidades JPA solo existen en infraestructura, el dominio no las ve. */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    protected UsuarioEntity() {
    }

    static UsuarioEntity desdeDominio(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        e.id = u.id();
        e.email = u.email();
        e.passwordHash = u.passwordHash();
        e.nombre = u.nombre();
        e.rol = u.rol();
        return e;
    }

    Usuario aDominio() {
        return new Usuario(id, email, passwordHash, nombre, rol);
    }
}
