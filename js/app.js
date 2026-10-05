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

function anadirAlCarrito(id, talla) {
  const carrito = leerCarrito();
  // Si ya existe el mismo producto con la misma talla, sumamos 1
  const existente = carrito.find(item => item.id === id && item.talla === talla);
  if (existente) {
    existente.cantidad++;
  } else {
    carrito.push({ id: id, talla: talla, cantidad: 1 });
  }
  guardarCarrito(carrito);
}

function cambiarCantidad(index, cambio) {
  const carrito = leerCarrito();
  carrito[index].cantidad += cambio;
  if (carrito[index].cantidad <= 0) carrito.splice(index, 1); // si llega a 0, se elimina
  guardarCarrito(carrito);
}

// Número total de prendas en el carrito (para el icono del menú)
function pintarContador() {
  const contador = document.getElementById("contador-carrito");
  if (!contador) return;
  const total = leerCarrito().reduce((suma, item) => suma + item.cantidad, 0);
  contador.textContent = total;
}

// ===== UTILIDADES =====

function formatearPrecio(numero) {
  return numero.toFixed(2).replace(".", ",") + " €";
}

// HTML de la "foto": usa la imagen si existe, si no un bloque de color
function htmlImagen(producto) {
  if (producto.imagen) {
    return `<img src="${producto.imagen}" alt="${producto.nombre}">`;
  }
  return `<div class="placeholder" style="background:${producto.color}"></div>`;
}

// HTML de una tarjeta de producto (se usa en inicio y catálogo)
function htmlTarjeta(producto) {
  return `
    <a class="tarjeta" href="producto.html?id=${producto.id}">
      ${htmlImagen(producto)}
      <h3>${producto.nombre}</h3>
      <p class="precio">${formatearPrecio(producto.precio)}</p>
    </a>`;
}

// Al cargar cualquier página, actualizamos el contador del carrito
document.addEventListener("DOMContentLoaded", pintarContador);
