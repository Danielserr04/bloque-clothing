// ===== CARRITO (se guarda en localStorage del navegador) =====
// Cada elemento del carrito: { id, talla, cantidad }

function leerCarrito() {
  try {
    return JSON.parse(localStorage.getItem("carrito")) || [];
  } catch (e) {
    return [];
  }
}

function guardarCarrito(carrito) {
  localStorage.setItem("carrito", JSON.stringify(carrito));
  pintarContador();
}

function anadirAlCarrito(id, talla, cantidad) {
  const carrito = leerCarrito();
  // Si ya existe el mismo producto con la misma talla, sumamos la cantidad
  const existente = carrito.find(item => item.id === id && item.talla === talla);
  if (existente) {
    existente.cantidad += cantidad;
  } else {
    carrito.push({ id: id, talla: talla, cantidad: cantidad });
  }
  guardarCarrito(carrito);
}

function cambiarCantidad(index, cambio) {
  const carrito = leerCarrito();
  carrito[index].cantidad += cambio;
  if (carrito[index].cantidad <= 0) carrito.splice(index, 1); // si llega a 0, se elimina
  guardarCarrito(carrito);
}

function quitarDelCarrito(index) {
  const carrito = leerCarrito();
  carrito.splice(index, 1);
  guardarCarrito(carrito);
}

// Número total de prendas en el carrito (para el botón del menú)
function pintarContador() {
  const contador = document.getElementById("contador-carrito");
  if (!contador) return;
  const total = leerCarrito().reduce((suma, item) => suma + item.cantidad, 0);
  contador.textContent = total;
  // Con prendas en el carro, el botón pasa a relleno blanco
  contador.parentElement.classList.toggle("lleno", total > 0);
}

// ===== UTILIDADES =====

function formatearPrecio(numero) {
  return numero.toFixed(2).replace(".", ",") + " €";
}

// Rellena con ceros a dos cifras: 8 -> "08"
function dosCifras(numero) {
  return String(numero).padStart(2, "0");
}

// HTML de la "foto": usa la imagen si existe, si no la retícula con el símbolo del producto
function htmlImagen(producto) {
  if (producto.imagen) {
    return `<img class="media" src="${producto.imagen}" alt="${producto.nombre}">`;
  }
  return `<div class="media reticula"><span class="simbolo">${producto.marca}</span></div>`;
}

// Etiqueta de stock que va encima de la imagen
function htmlEtiquetaStock(producto) {
  if (producto.stock === 0) return `<span class="tag tag-papel">Agotado</span>`;
  if (producto.stock <= 4) return `<span class="tag tag-senal">Últimas ${producto.stock}</span>`;
  return "";
}

// HTML de una tarjeta de producto (se usa en inicio y catálogo)
function htmlTarjeta(producto) {
  const meta = producto.stock === 0 ? "Agotado" : producto.tallas.join(" ");
  return `
    <a class="tarjeta" href="producto.html?id=${producto.id}">
      <div class="tarjeta-media">
        ${htmlImagen(producto)}
        <span class="tarjeta-tag">${htmlEtiquetaStock(producto)}</span>
      </div>
      <div class="tarjeta-cuerpo">
        <span class="kicker">${producto.seccion} / ${producto.ref}</span>
        <h3>${producto.nombre}</h3>
        <div class="tarjeta-pie">
          <span class="meta">${meta}</span>
          <span class="precio">${formatearPrecio(producto.precio)}</span>
        </div>
      </div>
    </a>`;
}

// ===== CABECERA, MARQUESINA Y PIE (iguales en todas las páginas) =====
// Cada página tiene <div id="cabecera" data-activa="..."></div> y <div id="pie"></div>

const AVISOS = ["Recogida en tienda en 2 h", "Reparto local 3,90 €", "Abierto de 9 a 20", "Calle Mayor 14"];

function pintarCabecera() {
  const caja = document.getElementById("cabecera");
  if (!caja) return;
  const activa = caja.dataset.activa; // "inicio", "catalogo" o "carrito"
  const enlace = (pagina, texto) =>
    `<a href="${pagina}.html" class="${pagina === activa ? "activo" : ""}">${texto}</a>`;

  // La marquesina se repite dos veces para que el bucle no tenga corte
  const avisos = [...AVISOS, ...AVISOS].map(a => `<span>${a}<i>✕</i></span>`).join("");

  caja.innerHTML = `
    <header class="nav">
      <a href="index.html" class="logo" aria-label="Bloque, inicio">
        <img src="img/isotipo.svg" alt="" width="18" height="18">
        <span>BLOQUE</span>
      </a>
      <nav>
        ${enlace("index", "Inicio")}
        ${enlace("catalogo", "Tienda")}
        <span class="nav-codigo">ES / 08014</span>
      </nav>
      <div class="nav-derecha">
        <a href="carrito.html" class="boton boton-sm boton-carro">Carro (<span id="contador-carrito">0</span>)</a>
      </div>
    </header>
    ${activa === "carrito" ? "" : `<div class="ticker"><div class="ticker-pista">${avisos}</div></div>`}`;
}

function pintarPie() {
  const caja = document.getElementById("pie");
  if (!caja) return;
  caja.innerHTML = `
    <footer class="pie">
      <div class="pie-columnas">
        <div><span class="label">Tienda</span><a href="catalogo.html">Novedades</a><a href="catalogo.html">Abrigos</a><a href="catalogo.html">Punto</a></div>
        <div><span class="label">Ayuda</span><span>Envíos</span><span>Cambios 30 días</span><span>Guía de tallas</span></div>
        <div><span class="label">Local</span><span>Calle Mayor 14</span><span>9:00 — 20:00</span><span>Instagram</span></div>
      </div>
      <div class="pie-barra label">
        <span>BLOQUE — Ropa y arreglos</span>
        <span>Calle Mayor 14, 08014</span>
        <span class="en-vivo">Abierto <i class="parpadeo">■</i></span>
      </div>
    </footer>`;
}

// ===== AVISO FLOTANTE (toast) =====
function mostrarAviso(codigo, titulo, texto) {
  let aviso = document.getElementById("aviso");
  if (!aviso) {
    aviso = document.createElement("div");
    aviso.id = "aviso";
    aviso.className = "toast";
    aviso.setAttribute("role", "status");
    document.body.appendChild(aviso);
  }
  aviso.innerHTML = `
    <span class="label">${codigo}</span>
    <strong>${titulo}</strong>
    <span>${texto}</span>
    <a href="carrito.html" class="boton boton-sm boton-tinta">Ver carro</a>`;
  aviso.classList.add("visible");
  clearTimeout(aviso.temporizador);
  aviso.temporizador = setTimeout(() => aviso.classList.remove("visible"), 4000);
}

// Al cargar cualquier página pintamos cabecera, pie y contador del carrito
document.addEventListener("DOMContentLoaded", () => {
  pintarCabecera();
  pintarPie();
  pintarContador();
});
