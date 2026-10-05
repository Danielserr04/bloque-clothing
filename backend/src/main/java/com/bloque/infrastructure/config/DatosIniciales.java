package com.bloque.infrastructure.config;

import com.bloque.application.port.out.PasswordHasherPort;
import com.bloque.application.port.out.ProductoRepositoryPort;
import com.bloque.application.port.out.UsuarioRepositoryPort;
import com.bloque.domain.model.Producto;
import com.bloque.domain.model.Rol;
import com.bloque.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Al arrancar:
 * - Crea el usuario ADMIN si se configuró (bloque.admin.email / bloque.admin.password) y no existe.
 * - Si bloque.cargar-productos-demo=true y no hay productos, carga los mismos de js/productos.js.
 */
@Component
class DatosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepositoryPort usuarios;
    private final ProductoRepositoryPort productos;
    private final PasswordHasherPort hasher;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean cargarProductosDemo;

    DatosIniciales(UsuarioRepositoryPort usuarios, ProductoRepositoryPort productos, PasswordHasherPort hasher,
                   @Value("${bloque.admin.email:}") String adminEmail,
                   @Value("${bloque.admin.password:}") String adminPassword,
                   @Value("${bloque.cargar-productos-demo:false}") boolean cargarProductosDemo) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.hasher = hasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.cargarProductosDemo = cargarProductosDemo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!adminEmail.isBlank() && !adminPassword.isBlank() && !usuarios.existeEmail(adminEmail.toLowerCase())) {
            usuarios.guardar(new Usuario(null, adminEmail, hasher.hash(adminPassword), "Admin", Rol.ADMIN));
            log.info("Usuario ADMIN creado: {}", adminEmail);
        }
        if (cargarProductosDemo && productos.listar().isEmpty()) {
            demo().forEach(productos::guardar);
            log.info("Productos de ejemplo cargados");
        }
    }

    private static List<Producto> demo() {
        return List.of(
                p("Camiseta Básica Negra", "camisetas", "19.99", "#222222", List.of("S", "M", "L", "XL"),
                        "Camiseta de algodón 100% con corte regular. Un básico que combina con todo."),
                p("Camiseta Oversize Blanca", "camisetas", "24.99", "#e9e6e0", List.of("S", "M", "L", "XL"),
                        "Corte amplio y tejido grueso para un look urbano."),
                p("Sudadera Logo Gris", "sudaderas", "44.99", "#8a8d91", List.of("S", "M", "L", "XL"),
                        "Sudadera con capucha, interior perchado y logo bordado."),
                p("Sudadera Crew Verde", "sudaderas", "39.99", "#4b5d48", List.of("M", "L", "XL"),
                        "Cuello redondo, tacto suave y color verde oliva."),
                p("Pantalón Cargo Beige", "pantalones", "49.99", "#c8b59a", List.of("38", "40", "42", "44"),
                        "Pantalón cargo con bolsillos laterales y ajuste relajado."),
                p("Gorra Clásica", "accesorios", "14.99", "#2f3e5c", List.of("Única"),
                        "Gorra de seis paneles con cierre ajustable."));
    }

    private static Producto p(String nombre, String cat, String precio, String color, List<String> tallas, String desc) {
        return new Producto(null, nombre, cat, new BigDecimal(precio), color, "", tallas, desc);
    }
}
