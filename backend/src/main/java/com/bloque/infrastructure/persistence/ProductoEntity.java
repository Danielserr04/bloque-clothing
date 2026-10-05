package com.bloque.infrastructure.persistence;

import com.bloque.domain.model.Producto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Tabla "productos" (+ tabla "producto_tallas" para la lista de tallas). */
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 20)
    private String color;

    @Column(length = 300)
    private String imagen;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_tallas", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    @Column(name = "talla", length = 20)
    private List<String> tallas = new ArrayList<>();

    @Column(length = 2000)
    private String descripcion;

    protected ProductoEntity() {
    }

    static ProductoEntity desdeDominio(Producto p) {
        ProductoEntity e = new ProductoEntity();
        e.id = p.id();
        e.nombre = p.nombre();
        e.categoria = p.categoria();
        e.precio = p.precio();
        e.color = p.color();
        e.imagen = p.imagen();
        e.tallas = new ArrayList<>(p.tallas());
        e.descripcion = p.descripcion();
        return e;
    }

    Producto aDominio() {
        return new Producto(id, nombre, categoria, precio, color, imagen, tallas, descripcion);
    }
}
