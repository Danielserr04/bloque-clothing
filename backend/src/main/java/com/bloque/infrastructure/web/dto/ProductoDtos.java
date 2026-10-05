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
            @Size(max = 10) String ref,
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 60) String seccion,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal precio,
            @NotNull @Min(0) @Max(100000) Integer stock,
            @Size(max = 4) String marca,
            @Size(max = 40) String color,
            @Size(max = 300) String imagen,
            @Size(max = 20) List<@NotBlank @Size(max = 20) String> tallas,
            @Size(max = 20) List<@NotBlank @Size(max = 20) String> agotadas,
            @Size(max = 2000) String descripcion) {

        public Producto aDominio() {
            return new Producto(null, ref, nombre, seccion, precio, stock, marca, color, imagen, tallas, agotadas,
                    descripcion);
        }
    }

    public record ProductoResponse(Long id, String ref, String nombre, String seccion, BigDecimal precio, int stock,
                                   String marca, String color, String imagen, List<String> tallas,
                                   List<String> agotadas, String descripcion) {
        public static ProductoResponse de(Producto p) {
            return new ProductoResponse(p.id(), p.ref(), p.nombre(), p.seccion(), p.precio(), p.stock(),
                    p.marca(), p.color(), p.imagen(), p.tallas(), p.agotadas(), p.descripcion());
        }
    }
}
