package com.bloque.infrastructure.web;

import com.bloque.application.port.in.ProductoUseCase;
import com.bloque.infrastructure.web.dto.ProductoDtos.ProductoRequest;
import com.bloque.infrastructure.web.dto.ProductoDtos.ProductoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador de ENTRADA: API de productos.
 * GET es público; POST/PUT/DELETE solo ADMIN (se controla en SecurityConfig).
 */
@RestController
@RequestMapping("/api/productos")
class ProductoController {

    private final ProductoUseCase productos;

    ProductoController(ProductoUseCase productos) {
        this.productos = productos;
    }

    @GetMapping
    List<ProductoResponse> listar() {
        return productos.listar().stream().map(ProductoResponse::de).toList();
    }

    @GetMapping("/{id}")
    ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.de(productos.obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return ProductoResponse.de(productos.crear(req.aDominio()));
    }

    @PutMapping("/{id}")
    ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return ProductoResponse.de(productos.actualizar(id, req.aDominio()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void borrar(@PathVariable Long id) {
        productos.borrar(id);
    }
}
