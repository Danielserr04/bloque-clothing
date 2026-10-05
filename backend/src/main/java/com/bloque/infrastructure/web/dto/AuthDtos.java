package com.bloque.infrastructure.web.dto;

import com.bloque.domain.model.Tokens;
import com.bloque.domain.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs = lo que entra y sale por la API en JSON.
 * Ojo: el registro NO tiene campo "rol", así nadie puede mandarse rol ADMIN.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password,
            @Size(max = 100) String nombre) {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password) {
    }

    public record RefreshRequest(@NotBlank @Size(max = 200) String refreshToken) {
    }

    public record TokensResponse(String accessToken, String refreshToken, String tipo, long expiraEn) {
        public static TokensResponse de(Tokens t) {
            return new TokensResponse(t.accessToken(), t.refreshToken(), "Bearer", t.expiraEnSegundos());
        }
    }

    /** Datos públicos del usuario: nunca devolvemos el hash de la contraseña. */
    public record UsuarioResponse(Long id, String email, String nombre, String rol) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.id(), u.email(), u.nombre(), u.rol().name());
        }
    }
}
