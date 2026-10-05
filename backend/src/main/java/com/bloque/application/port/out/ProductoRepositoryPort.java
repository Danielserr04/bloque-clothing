package com.bloque.application.port.out;

import com.bloque.domain.model.Producto;

import java.util.List;
import java.util.Optional;

/** Puerto de SALIDA: almacenamiento de productos. */
public interface ProductoRepositoryPort {

    List<Producto> listar();

    Optional<Producto> buscarPorId(Long id);

    Producto guardar(Producto producto);

    boolean existe(Long id);

    void borrar(Long id);
}
