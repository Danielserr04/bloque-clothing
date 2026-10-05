package com.bloque.infrastructure.persistence;

import com.bloque.domain.model.Producto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Tabla "productos" (+ tablas "producto_tallas" y "producto_tallas_agotadas"). */
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String ref;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 60)
    private String seccion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int stock;

    @Column(length = 4)
    private String marca;

    @Column(length = 40)
    private String color;

    @Column(length = 300)
    private String imagen;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_tallas", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    @Column(name = "talla", length = 20)
    private List<String> tallas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_tallas_agotadas", joinColumns = @JoinColumn(name = "producto_id"))
    @OrderColumn(name = "orden")
    @Column(name = "talla", length = 20)
    private List<String> agotadas = new ArrayList<>();

    @Column(length = 2000)
    private String descripcion;

    protected ProductoEntity() {
    }

    static ProductoEntity desdeDominio(Producto p) {
        ProductoEntity e = new ProductoEntity();
        e.id = p.id();
        e.ref = p.ref();
        e.nombre = p.nombre();
        e.seccion = p.seccion();
        e.precio = p.precio();
        e.stock = p.stock();
        e.marca = p.marca();
        e.color = p.color();
        e.imagen = p.imagen();
        e.tallas = new ArrayList<>(p.tallas());
        e.agotadas = new ArrayList<>(p.agotadas());
        e.descripcion = p.descripcion();
        return e;
    }

    Producto aDominio() {
        return new Producto(id, ref, nombre, seccion, precio, stock, marca, color, imagen, tallas, agotadas, descripcion);
    }
}
