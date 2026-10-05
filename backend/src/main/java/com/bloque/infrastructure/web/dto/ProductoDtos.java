package com.bloque.infrastructure.web.dto;

import com.bloque.domain.model.Producto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** JSON de entrada/salida de productos (mismos campos que js/productos.js del frontend). */
public final class ProductoDtos {

    private ProductoDtos() {
    }

    public record ProductoRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 60) String categoria,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal precio,
            @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "debe ser un color tipo #1a2b3c") String color,
            @Size(max = 300) String imagen,
            @Size(max = 20) List<@NotBlank @Size(max = 20) String> tallas,
            @Size(max = 2000) String descripcion) {

        public Producto aDominio() {
            return new Producto(null, nombre, categoria, precio, color, imagen, tallas, descripcion);
        }
    }

    public record ProductoResponse(Long id, String nombre, String categoria, BigDecimal precio,
                                   String color, String imagen, List<String> tallas, String descripcion) {
        public static ProductoResponse de(Producto p) {
            return new ProductoResponse(p.id(), p.nombre(), p.categoria(), p.precio(),
                    p.color(), p.imagen(), p.tallas(), p.descripcion());
        }
    }
}
