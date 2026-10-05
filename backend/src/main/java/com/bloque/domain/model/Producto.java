package com.bloque.domain.model;

import com.bloque.domain.exception.DatosInvalidosException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Producto del catálogo (mismos campos que js/productos.js del frontend).
 * Las reglas de negocio (precio > 0, stock no negativo...) viven aquí,
 * así se cumplen venga la petición de donde venga.
 *
 * - ref:      código visible del producto, ej. "0042"
 * - seccion:  para los filtros del catálogo, ej. "Camisetas"
 * - stock:    unidades disponibles (0 = agotado)
 * - marca:    símbolo que se dibuja cuando no hay foto, ej. "■"
 * - agotadas: tallas sin stock (deben estar dentro de "tallas")
 */
public record Producto(Long id, String ref, String nombre, String seccion, BigDecimal precio, int stock,
                       String marca, String color, String imagen, List<String> tallas, List<String> agotadas,
                       String descripcion) {

    public Producto {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre es obligatorio");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new DatosInvalidosException("El precio debe ser mayor que 0");
        }
        if (stock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo");
        }
        tallas = tallas == null ? List.of() : List.copyOf(tallas);
        agotadas = agotadas == null ? List.of() : List.copyOf(agotadas);
        if (!tallas.containsAll(agotadas)) {
            throw new DatosInvalidosException("Las tallas agotadas tienen que estar en la lista de tallas");
        }
    }

    /** Devuelve una copia con otro id (útil al guardar o actualizar). */
    public Producto conId(Long nuevoId) {
        return new Producto(nuevoId, ref, nombre, seccion, precio, stock, marca, color, imagen, tallas, agotadas,
                descripcion);
    }
}
