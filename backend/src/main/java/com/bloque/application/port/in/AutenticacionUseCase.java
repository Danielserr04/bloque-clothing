package com.bloque.application.port.in;

import com.bloque.domain.model.Tokens;
import com.bloque.domain.model.Usuario;

/**
 * Puerto de ENTRADA: lo que la app sabe hacer con la autenticación.
 * Los controladores REST llaman a esta interfaz, no a la implementación.
 */
public interface AutenticacionUseCase {

    Usuario registrar(String email, String password, String nombre);

    Tokens login(String email, String password);

    /** Cambia un refresh token válido por un par nuevo (el viejo deja de valer). */
    Tokens refrescar(String refreshToken);

    /** Invalida el refresh token (cerrar sesión). */
    void logout(String refreshToken);
}
