// Catálogo de productos de ejemplo.
// Para añadir un producto, copia un bloque { ... } y cambia sus datos.
// - ref:       código del producto (se muestra como "REF. 0042")
// - seccion:   se usa para los filtros del catálogo
// - stock:     unidades disponibles (0 = agotado)
// - agotadas:  tallas sin stock (salen tachadas)
// - marca:     símbolo que se dibuja cuando no hay foto
// - imagen:    ruta a una foto en /img (si está vacía se muestra la retícula con el símbolo)
const SECCIONES = ["Todo", "Camisetas", "Pantalones", "Abrigos", "Punto", "Calzado"];

const PRODUCTOS = [
  { id: 1, ref: "0042", nombre: "Camiseta caja 240g", seccion: "Camisetas", precio: 28.00, stock: 12, marca: "■", color: "Negro", imagen: "", tallas: ["S", "M", "L", "XL"], agotadas: ["S"], descripcion: "Algodón peinado 240 g, corte caja, cuello reforzado." },
  { id: 2, ref: "0043", nombre: "Pantalón carpenter", seccion: "Pantalones", precio: 74.00, stock: 4, marca: "▮", color: "Crudo", imagen: "", tallas: ["38", "40", "42", "44"], agotadas: [], descripcion: "Lona de algodón 12 oz, pierna recta, martillera lateral." },
  { id: 3, ref: "0055", nombre: "Parka técnica", seccion: "Abrigos", precio: 189.00, stock: 2, marca: "▲", color: "Negro", imagen: "", tallas: ["M", "L", "XL"], agotadas: ["XL"], descripcion: "Nylon recubierto, costuras selladas, capucha ajustable." },
  { id: 4, ref: "0061", nombre: "Jersey lana gruesa", seccion: "Punto", precio: 96.00, stock: 6, marca: "●", color: "Gris", imagen: "", tallas: ["S", "M", "L"], agotadas: [], descripcion: "Lana virgen, punto inglés, cuello alto." },
  { id: 5, ref: "0072", nombre: "Bota de cuero", seccion: "Calzado", precio: 168.00, stock: 0, marca: "◆", color: "Negro", imagen: "", tallas: ["41", "42", "43", "44"], agotadas: ["41", "42", "43", "44"], descripcion: "Cuero engrasado, suela cosida, horma ancha." },
  { id: 6, ref: "0080", nombre: "Camisa overshirt", seccion: "Camisetas", precio: 82.00, stock: 8, marca: "▬", color: "Verde", imagen: "", tallas: ["S", "M", "L", "XL"], agotadas: [], descripcion: "Sarga de algodón, doble bolsillo, botón de corozo." },
  { id: 7, ref: "0091", nombre: "Vaquero rígido", seccion: "Pantalones", precio: 110.00, stock: 3, marca: "▯", color: "Índigo", imagen: "", tallas: ["38", "40", "42"], agotadas: [], descripcion: "Denim selvedge 14 oz sin lavar. Encoge media talla." },
  { id: 8, ref: "0103", nombre: "Gorro punto", seccion: "Punto", precio: 24.00, stock: 20, marca: "◐", color: "Negro", imagen: "", tallas: ["Única"], agotadas: [], descripcion: "Lana merino, vuelta doble." }
];

// Devuelve un producto a partir de su id
function buscarProducto(id) {
  return PRODUCTOS.find(p => p.id === Number(id));
}
