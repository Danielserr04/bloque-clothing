package com.bloque.domain.exception;

/** Email o contraseña incorrectos, o refresh token no válido (401). */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
