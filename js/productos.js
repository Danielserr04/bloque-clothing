// Catálogo de productos de ejemplo.
// Para añadir un producto, copia un bloque { ... } y cambia sus datos.
// "imagen": ruta a una foto en /img (si está vacía se muestra un color de fondo).
const PRODUCTOS = [
  { id: 1, nombre: "Camiseta Básica Negra", categoria: "camisetas", precio: 19.99, color: "#222222", imagen: "", tallas: ["S", "M", "L", "XL"], descripcion: "Camiseta de algodón 100% con corte regular. Un básico que combina con todo." },
  { id: 2, nombre: "Camiseta Oversize Blanca", categoria: "camisetas", precio: 24.99, color: "#e9e6e0", imagen: "", tallas: ["S", "M", "L", "XL"], descripcion: "Corte amplio y tejido grueso para un look urbano." },
  { id: 3, nombre: "Sudadera Logo Gris", categoria: "sudaderas", precio: 44.99, color: "#8a8d91", imagen: "", tallas: ["S", "M", "L", "XL"], descripcion: "Sudadera con capucha, interior perchado y logo bordado." },
  { id: 4, nombre: "Sudadera Crew Verde", categoria: "sudaderas", precio: 39.99, color: "#4b5d48", imagen: "", tallas: ["M", "L", "XL"], descripcion: "Cuello redondo, tacto suave y color verde oliva." },
  { id: 5, nombre: "Pantalón Cargo Beige", categoria: "pantalones", precio: 49.99, color: "#c8b59a", imagen: "", tallas: ["38", "40", "42", "44"], descripcion: "Pantalón cargo con bolsillos laterales y ajuste relajado." },
  { id: 6, nombre: "Gorra Clásica", categoria: "accesorios", precio: 14.99, color: "#2f3e5c", imagen: "", tallas: ["Única"], descripcion: "Gorra de seis paneles con cierre ajustable." }
];

// Devuelve un producto a partir de su id
function buscarProducto(id) {
  return PRODUCTOS.find(p => p.id === Number(id));
}
