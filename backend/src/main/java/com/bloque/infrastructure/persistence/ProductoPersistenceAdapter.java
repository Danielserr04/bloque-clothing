package com.bloque.infrastructure.persistence;

import com.bloque.application.port.out.ProductoRepositoryPort;
import com.bloque.domain.model.Producto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de SALIDA: implementa el puerto de productos usando JPA. */
@Component
class ProductoPersistenceAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository repo;

    ProductoPersistenceAdapter(ProductoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Producto> listar() {
        return repo.findAll().stream().map(ProductoEntity::aDominio).toList();
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return repo.findById(id).map(ProductoEntity::aDominio);
    }

    @Override
    public Producto guardar(Producto producto) {
        return repo.save(ProductoEntity.desdeDominio(producto)).aDominio();
    }

    @Override
    public boolean existe(Long id) {
        return repo.existsById(id);
    }

    @Override
    public void borrar(Long id) {
        repo.deleteById(id);
    }
}
