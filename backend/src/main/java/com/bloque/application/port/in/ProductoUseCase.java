package com.bloque.application.port.in;

import com.bloque.domain.model.Producto;

import java.util.List;

/** Puerto de ENTRADA: casos de uso del catálogo. */
public interface ProductoUseCase {

    List<Producto> listar();

    Producto obtener(Long id);

    Producto crear(Producto producto);

    Producto actualizar(Long id, Producto producto);

    void borrar(Long id);
}
