# Bloque · Tienda online

Web estática (HTML, CSS y JavaScript sin frameworks).

## Cómo abrirla
Doble clic en `index.html` y se abre en el navegador. No hace falta servidor.

## Archivos
- `index.html` portada con productos destacados
- `catalogo.html` todos los productos con filtro por categoría
- `producto.html?id=1` ficha de un producto (elegir talla y añadir)
- `carrito.html` carrito (se guarda en el navegador con localStorage)
- `js/productos.js` **aquí editas tus productos** (nombre, precio, tallas, foto)
- `js/app.js` funciones del carrito, cabecera, pie y aviso flotante (compartidas por todas las páginas)
- `css/styles.css` estilos de la tienda
- `css/tokens/` variables del design system BLOQUE (colores, fuentes, espacios). Cámbialas aquí y se aplican en toda la web
- `img/` mete aquí las fotos y pon la ruta en `imagen`, ej. `"img/camiseta.jpg"`

## Pendiente
- Pago con Stripe
- Fotos reales de producto (alto contraste, fondo negro, según el design system)
