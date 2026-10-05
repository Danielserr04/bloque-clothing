# Bloque · Tienda online

Web estática (HTML, CSS y JavaScript sin frameworks).

## Cómo abrirla

**Con backend (lo normal):**
1. Arranca la API: `cd backend && ./mvnw spring-boot:run` (necesitas Java 21). Ver [backend/README.md](backend/README.md).
2. Abre la web con **Live Server** de VS Code (clic derecho en `index.html` → *Open with Live Server*, sale en `http://127.0.0.1:5500`).

Los productos, el login y el registro van contra la API. Usuario admin de desarrollo: `admin@bloque.local` / `Admin1234`.

**Sin backend:** doble clic en `index.html`. La web usa los productos de respaldo de `js/productos.js`; el login no funciona.
(Con doble clic la página se abre como `file://` y el navegador bloquea las llamadas a la API por CORS.)

## Archivos
- `index.html` portada con productos destacados
- `catalogo.html` todos los productos con filtro por categoría
- `producto.html?id=1` ficha de un producto (elegir talla y añadir)
- `carrito.html` carrito (se guarda en el navegador con localStorage). Para tramitar el pedido hay que entrar
- `cuenta.html` entrar, crear cuenta y cerrar sesión (JWT contra el backend)
- `js/api.js` llamadas al backend: productos, login, registro, refresh del token y logout
- `js/productos.js` productos de respaldo para cuando el backend no está arrancado
- `js/app.js` funciones del carrito, cabecera, pie y aviso flotante (compartidas por todas las páginas)
- `css/styles.css` estilos de la tienda
- `css/tokens/` variables del design system BLOQUE (colores, fuentes, espacios). Cámbialas aquí y se aplican en toda la web
- `img/` mete aquí las fotos y pon la ruta en `imagen`, ej. `"img/camiseta.jpg"`
- `backend/` API en Spring Boot (hexagonal + JWT)

## Seguridad en la web
- Todo lo que llega de la API pasa por `escapar()` antes de ir al HTML, para que un nombre de producto con `<script>` no se ejecute (XSS).
- Los tokens se guardan en `sessionStorage` (se borran al cerrar la pestaña). Si el access token caduca, `js/api.js` pide uno nuevo con el refresh token.
- Pendiente: mover el refresh token a una cookie `httpOnly` para que JavaScript no pueda leerlo.

## Pendiente
- Pago con Stripe
- Panel de admin para crear y editar productos desde la web (la API ya lo permite)
- Fotos reales de producto (alto contraste, fondo negro, según el design system)
