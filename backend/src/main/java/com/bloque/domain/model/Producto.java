package com.bloque.domain.model;

import com.bloque.domain.exception.DatosInvalidosException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Producto del catálogo. Las reglas de negocio (precio > 0, nombre obligatorio...)
 * viven aquí, así se cumplen venga la petición de donde venga.
 */
public record Producto(Long id, String nombre, String categoria, BigDecimal precio,
                       String color, String imagen, List<String> tallas, String descripcion) {

    public Producto {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre es obligatorio");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new DatosInvalidosException("El precio debe ser mayor que 0");
        }
        tallas = tallas == null ? List.of() : List.copyOf(tallas);
    }

    /** Devuelve una copia con otro id (útil al guardar o actualizar). */
    public Producto conId(Long nuevoId) {
        return new Producto(nuevoId, nombre, categoria, precio, color, imagen, tallas, descripcion);
    }
}
