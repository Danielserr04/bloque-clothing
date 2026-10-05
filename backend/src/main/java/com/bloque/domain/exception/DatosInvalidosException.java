package com.bloque.domain.exception;

/** Datos de entrada no válidos (400). */
public class DatosInvalidosException extends RuntimeException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
