# Backend de Bloque

API REST de la tienda: **Spring Boot 3 + Java 21 + arquitectura hexagonal + JWT**.

## Arrancar

Solo necesitas Java 21 (Maven viene incluido con `mvnw`).

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Arranca en `http://localhost:8080` con el perfil **dev**: base de datos H2 en memoria (se borra al parar),
los 6 productos de ejemplo y un admin `admin@bloque.local` / `Admin1234` (solo en dev).

Para probar la API a mano: abre [`peticiones.http`](peticiones.http) (extensión "REST Client" en VS Code).

## Probar la seguridad

```bash
./mvnw test
```

- `SeguridadApiTest`: ataques contra la API real (token falso, `alg: none`, rol cambiado a mano,
  token caducado, fuerza bruta, CORS, registrarse como ADMIN...). Si un test falla, hay un agujero.
- `AutenticacionServiceTest`: reglas de login/registro sin Spring ni BD (gracias a la hexagonal).

## Endpoints

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/auth/registro` | público |
| POST | `/api/auth/login` | público → devuelve `accessToken` y `refreshToken` |
| POST | `/api/auth/refresh` | público (con refresh token) |
| POST | `/api/auth/logout` | público (con refresh token) |
| GET | `/api/usuarios/me` | con token |
| GET | `/api/productos`, `/api/productos/{id}` | público |
| POST/PUT/DELETE | `/api/productos/...` | solo ADMIN |

El token se manda así: `Authorization: Bearer <accessToken>`.

## Estructura (hexagonal)

```
com.bloque
├── domain          Modelos y reglas de negocio. Java puro, sin Spring.
├── application
│   ├── port.in     Lo que la app sabe hacer (casos de uso).
│   ├── port.out    Lo que la app necesita de fuera (BD, tokens, hash...).
│   └── service     Implementación de los casos de uso.
└── infrastructure  Adaptadores: web (controladores), persistence (JPA), security (JWT, BCrypt), config.
```

Regla: las flechas solo apuntan hacia dentro. `domain` no conoce a nadie; `infrastructure` conoce a todos.

## Qué medidas de seguridad tiene

- Contraseñas con **BCrypt** (coste 12). Mínimo 8 caracteres con letra y número.
- **Access token** JWT HS256 que dura 15 min. Se valida firma, algoritmo, caducidad, emisor y que tenga rol.
- **Refresh token** aleatorio (no JWT) que dura 7 días, guardado en BD **solo como hash**, de un solo uso (rotación) e invalidable con logout.
- **Bloqueo por fuerza bruta**: 5 fallos seguidos en un email → 15 min bloqueado (429).
- Mismo error si el email no existe o la contraseña está mal (no se pueden adivinar emails registrados).
- Nadie puede registrarse como ADMIN. Rutas no previstas → denegadas.
- CORS solo para los orígenes configurados, cabeceras de seguridad (CSP, HSTS, nosniff, X-Frame-Options).
- Errores sin trazas internas.

## Producción

Perfil `prod` con PostgreSQL. Todo sale de variables de entorno (si falta una, no arranca):

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://host:5432/bloque
DB_USER=...
DB_PASSWORD=...
JWT_SECRET=...          # mínimo 32 caracteres aleatorios: openssl rand -base64 48
CORS_ORIGENES=https://tu-dominio.com
ADMIN_EMAIL=...         # opcional: crea el admin la primera vez
ADMIN_PASSWORD=...
```

Pendiente para más adelante: migraciones con Flyway, y mover el bloqueo de intentos a Redis si hay varios servidores.
