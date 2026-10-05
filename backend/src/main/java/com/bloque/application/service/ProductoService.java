package com.bloque.application.service;

import com.bloque.application.port.in.ProductoUseCase;
import com.bloque.application.port.out.ProductoRepositoryPort;
import com.bloque.domain.exception.ProductoNoEncontradoException;
import com.bloque.domain.model.Producto;

import java.util.List;

/** Casos de uso del catálogo. Quién puede llamar a cada uno lo decide la capa de seguridad. */
public class ProductoService implements ProductoUseCase {

    private final ProductoRepositoryPort productos;

    public ProductoService(ProductoRepositoryPort productos) {
        this.productos = productos;
    }

    @Override
    public List<Producto> listar() {
        return productos.listar();
    }

    @Override
    public Producto obtener(Long id) {
        return productos.buscarPorId(id).orElseThrow(() -> noEncontrado(id));
    }

    @Override
    public Producto crear(Producto producto) {
        // Ignoramos cualquier id que venga del cliente: lo asigna la BD.
        return productos.guardar(producto.conId(null));
    }

    @Override
    public Producto actualizar(Long id, Producto producto) {
        if (!productos.existe(id)) {
            throw noEncontrado(id);
        }
        return productos.guardar(producto.conId(id));
    }

    @Override
    public void borrar(Long id) {
        if (!productos.existe(id)) {
            throw noEncontrado(id);
        }
        productos.borrar(id);
    }

    private static ProductoNoEncontradoException noEncontrado(Long id) {
        return new ProductoNoEncontradoException("No existe el producto " + id);
    }
}
