package com.bloque.domain.exception;

/** Demasiados logins fallidos seguidos (429). */
public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
