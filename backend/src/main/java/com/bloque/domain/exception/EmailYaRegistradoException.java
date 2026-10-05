package com.bloque.domain.exception;

/** El email ya existe (409). */
public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
