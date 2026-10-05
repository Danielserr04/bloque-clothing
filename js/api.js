// ===== CONEXIÓN CON EL BACKEND (Spring Boot en backend/) =====
// Para que funcione:
//   1. Arranca el backend:   cd backend && ./mvnw spring-boot:run
//   2. Abre la web con Live Server de VS Code (http://127.0.0.1:5500).
//      Con doble clic (file://) el navegador bloquea las llamadas por CORS
//      y la web usa los productos de js/productos.js como respaldo.

const API_URL = "http://localhost:8080/api";

// ----- Sesión -----
// Guardamos los tokens en sessionStorage: se borran al cerrar la pestaña.
// (Más adelante se puede pasar el refresh token a una cookie httpOnly, que es más seguro.)

function leerSesion() {
  try {
    return JSON.parse(sessionStorage.getItem("sesion"));
  } catch (e) {
    return null;
  }
}

function guardarSesion(tokens) {
  sessionStorage.setItem("sesion", JSON.stringify({
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken
  }));
}

function borrarSesion() {
  sessionStorage.removeItem("sesion");
}

function haySesion() {
  return leerSesion() !== null;
}

// ----- Llamada genérica a la API -----
// Añade el token si hay sesión. Si el access token ha caducado (401),
// pide uno nuevo con el refresh token y repite la llamada una vez.
async function llamarApi(ruta, opciones = {}, reintentar = true) {
  const sesion = leerSesion();
  const cabeceras = { "Content-Type": "application/json" };
  if (sesion) cabeceras["Authorization"] = "Bearer " + sesion.accessToken;

  const respuesta = await fetch(API_URL + ruta, { ...opciones, headers: cabeceras });

  if (respuesta.status === 401 && sesion && reintentar) {
    const renovado = await renovarTokens();
    if (renovado) return llamarApi(ruta, opciones, false);
  }
  return respuesta;
}

async function renovarTokens() {
  const sesion = leerSesion();
  if (!sesion) return false;
  const respuesta = await fetch(API_URL + "/auth/refresh", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: sesion.refreshToken })
  });
  if (!respuesta.ok) {
    borrarSesion(); // el refresh también ha caducado: hay que volver a entrar
    return false;
  }
  guardarSesion(await respuesta.json());
  return true;
}

// Saca el mensaje de error que manda el backend (formato ProblemDetail: { detail, errores })
async function mensajeDeError(respuesta) {
  try {
    const datos = await respuesta.json();
    // Errores de validación campo a campo, ej. { email: "must be a well-formed email address" }
    if (datos.errores) return datos.detail + ": " + Object.keys(datos.errores).join(", ");
    return datos.detail || "Algo ha fallado";
  } catch (e) {
    return "Algo ha fallado";
  }
}

// ----- Productos -----
// Pide el catálogo al backend y lo guarda en PRODUCTOS.
// Si el backend no responde, nos quedamos con los de js/productos.js.
async function cargarProductos() {
  try {
    const respuesta = await fetch(API_URL + "/productos");
    if (!respuesta.ok) throw new Error("HTTP " + respuesta.status);
    PRODUCTOS = await respuesta.json();
  } catch (e) {
    console.warn("Backend no disponible, uso los productos locales.", e.message);
  }
  return PRODUCTOS;
}

// ----- Cuenta -----

// Igual que fetch, pero si no hay conexión con el backend lanza un error entendible
async function fetchSeguro(url, opciones) {
  try {
    return await fetch(url, opciones);
  } catch (e) {
    throw new Error("No hay conexión con el servidor");
  }
}

async function entrar(email, password) {
  const respuesta = await fetchSeguro(API_URL + "/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });
  if (!respuesta.ok) throw new Error(await mensajeDeError(respuesta));
  guardarSesion(await respuesta.json());
}

async function registrarse(nombre, email, password) {
  const respuesta = await fetchSeguro(API_URL + "/auth/registro", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nombre, email, password })
  });
  if (!respuesta.ok) throw new Error(await mensajeDeError(respuesta));
  // Tras registrarse, entramos directamente
  await entrar(email, password);
}

async function salir() {
  const sesion = leerSesion();
  if (sesion) {
    // Avisamos al backend para que invalide el refresh token (aunque falle, borramos la sesión)
    try {
      await fetch(API_URL + "/auth/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken: sesion.refreshToken })
      });
    } catch (e) { /* sin conexión: da igual */ }
  }
  borrarSesion();
}

// Devuelve { id, email, roles } del usuario con sesión, o null
async function quienSoy() {
  if (!haySesion()) return null;
  const respuesta = await llamarApi("/usuarios/me");
  if (!respuesta.ok) {
    borrarSesion();
    return null;
  }
  return respuesta.json();
}
