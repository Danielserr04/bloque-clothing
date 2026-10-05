package com.bloque.domain.exception;

/** No existe el producto pedido (404). */
public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
