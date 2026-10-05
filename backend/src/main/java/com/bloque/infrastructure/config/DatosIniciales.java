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
                p("0042", "Camiseta caja 240g", "Camisetas", "28.00", 12, "\u25A0", "Negro",
                        List.of("S", "M", "L", "XL"), List.of("S"), "Algodón peinado 240 g, corte caja, cuello reforzado."),
                p("0043", "Pantalón carpenter", "Pantalones", "74.00", 4, "\u25AE", "Crudo",
                        List.of("38", "40", "42", "44"), List.of(), "Lona de algodón 12 oz, pierna recta, martillera lateral."),
                p("0055", "Parka técnica", "Abrigos", "189.00", 2, "\u25B2", "Negro",
                        List.of("M", "L", "XL"), List.of("XL"), "Nylon recubierto, costuras selladas, capucha ajustable."),
                p("0061", "Jersey lana gruesa", "Punto", "96.00", 6, "\u25CF", "Gris",
                        List.of("S", "M", "L"), List.of(), "Lana virgen, punto inglés, cuello alto."),
                p("0072", "Bota de cuero", "Calzado", "168.00", 0, "\u25C6", "Negro",
                        List.of("41", "42", "43", "44"), List.of("41", "42", "43", "44"), "Cuero engrasado, suela cosida, horma ancha."),
                p("0080", "Camisa overshirt", "Camisetas", "82.00", 8, "\u25AC", "Verde",
                        List.of("S", "M", "L", "XL"), List.of(), "Sarga de algodón, doble bolsillo, botón de corozo."),
                p("0091", "Vaquero rígido", "Pantalones", "110.00", 3, "\u25AF", "Índigo",
                        List.of("38", "40", "42"), List.of(), "Denim selvedge 14 oz sin lavar. Encoge media talla."),
                p("0103", "Gorro punto", "Punto", "24.00", 20, "\u25D0", "Negro",
                        List.of("Única"), List.of(), "Lana merino, vuelta doble."));
    }

    private static Producto p(String ref, String nombre, String seccion, String precio, int stock, String marca,
                              String color, List<String> tallas, List<String> agotadas, String desc) {
        return new Producto(null, ref, nombre, seccion, new BigDecimal(precio), stock, marca, color, "", tallas,
                agotadas, desc);
    }
}
