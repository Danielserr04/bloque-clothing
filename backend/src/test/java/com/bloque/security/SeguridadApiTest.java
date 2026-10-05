package com.bloque.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de SEGURIDAD de la API completa (arranca Spring con H2).
 * Cada test es un "ataque" o una regla: si alguien rompe la seguridad, el test falla.
 * Lanzar con: ./mvnw test   (o mvn test)
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadApiTest {

    private static final String ADMIN_EMAIL = "admin@bloque.local";
    private static final String ADMIN_PASS = "Admin1234";
    private static final String PRODUCTO_JSON = """
            {"ref":"9999","nombre":"Camiseta Test","seccion":"Camisetas","precio":19.99,"stock":5,
             "marca":"■","color":"Negro","tallas":["M","L"],"agotadas":["L"],"descripcion":"test"}""";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtEncoder encoderReal;

    // ---------- Helpers ----------

    private String emailNuevo() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    /** Petición POST con cuerpo JSON (sin enviarla todavía). */
    private static MockHttpServletRequestBuilder jsonPost(String url, String body) {
        return MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions post(String url, String body) throws Exception {
        return mvc.perform(jsonPost(url, body));
    }

    private JsonNode login(String email, String pass) throws Exception {
        String res = post("/api/auth/login", "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, pass))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    private String tokenAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASS).get("accessToken").asText();
    }

    private String tokenUserNuevo() throws Exception {
        String email = emailNuevo();
        post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(email))
                .andExpect(status().isCreated());
        return login(email, "secreta123").get("accessToken").asText();
    }

    /** Crea un JWT a mano con el encoder y los datos que queramos (para simular ataques). */
    private String jwt(JwtEncoder encoder, String issuer, Instant exp, List<String> roles) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer).subject("999").issuedAt(exp.minusSeconds(900)).expiresAt(exp)
                .claim("email", "hacker@test.com");
        if (roles != null) {
            claims.claim("roles", roles);
        }
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
                .getTokenValue();
    }

    private ResultActions crearProductoCon(String token) throws Exception {
        return mvc.perform(jsonPost("/api/productos", PRODUCTO_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Permisos por rol ----------

    @Nested
    class Permisos {

        @Test
        void catalogoEsPublico() throws Exception {
            mvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(greaterThan(0))));
        }

        @Test
        void crearProductoSinTokenDa401() throws Exception {
            post("/api/productos", PRODUCTO_JSON).andExpect(status().isUnauthorized());
        }

        @Test
        void crearProductoComoUserDa403() throws Exception {
            crearProductoCon(tokenUserNuevo()).andExpect(status().isForbidden());
        }

        @Test
        void borrarProductoComoUserDa403() throws Exception {
            mvc.perform(delete("/api/productos/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUserNuevo()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void crearProductoComoAdminDa201() throws Exception {
            crearProductoCon(tokenAdmin()).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        void meRequiereToken() throws Exception {
            mvc.perform(get("/api/usuarios/me")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUserNuevo()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("USER"));
        }

        @Test
        void rutasNoPrevistasEstanDenegadas() throws Exception {
            mvc.perform(get("/h2-console")).andExpect(status().is4xxClientError());
            mvc.perform(get("/actuator")).andExpect(status().is4xxClientError());
        }
    }

    // ---------- Ataques al JWT ----------

    @Nested
    class AtaquesJwt {

        @Test
        void tokenBasuraDa401() throws Exception {
            crearProductoCon("esto.no.es-un-jwt").andExpect(status().isUnauthorized());
        }

        @Test
        void tokenFirmadoConOtraClaveDa401() throws Exception {
            SecretKey otraClave = new SecretKeySpec("una-clave-distinta-que-no-es-la-buena-1234".getBytes(), "HmacSHA256");
            String falso = jwt(new NimbusJwtEncoder(new ImmutableSecret<>(otraClave)), "bloque-api",
                    Instant.now().plusSeconds(600), List.of("ADMIN"));
            crearProductoCon(falso).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenConAlgNoneDa401() throws Exception {
            String header = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}");
            String payload = b64("{\"iss\":\"bloque-api\",\"sub\":\"1\",\"roles\":[\"ADMIN\"],\"exp\":"
                    + Instant.now().plusSeconds(600).getEpochSecond() + "}");
            crearProductoCon(header + "." + payload + ".").andExpect(status().isUnauthorized());
        }

        @Test
        void cambiarElRolEnElPayloadRompeLaFirma() throws Exception {
            String[] partes = tokenUserNuevo().split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                    .replace("\"USER\"", "\"ADMIN\"");
            String manipulado = partes[0] + "." + b64(payload) + "." + partes[2];
            crearProductoCon(manipulado).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenCaducadoDa401() throws Exception {
            String caducado = jwt(encoderReal, "bloque-api", Instant.now().minusSeconds(3600), List.of("ADMIN"));
            crearProductoCon(caducado).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenDeOtroEmisorDa401() throws Exception {
            String otroIss = jwt(encoderReal, "otra-app", Instant.now().plusSeconds(600), List.of("ADMIN"));
            crearProductoCon(otroIss).andExpect(status().isUnauthorized());
        }

        @Test
        void tokenSinRolesDa401() throws Exception {
            String sinRoles = jwt(encoderReal, "bloque-api", Instant.now().plusSeconds(600), null);
            crearProductoCon(sinRoles).andExpect(status().isUnauthorized());
        }

        @Test
        void elRefreshTokenNoSirveComoAccessToken() throws Exception {
            String email = emailNuevo();
            post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(email));
            String refresh = login(email, "secreta123").get("refreshToken").asText();
            mvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + refresh))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------- Registro y login ----------

    @Nested
    class RegistroYLogin {

        @Test
        void noPuedesRegistrarteComoAdmin() throws Exception {
            String email = emailNuevo();
            post("/api/auth/registro",
                    "{\"email\":\"%s\",\"password\":\"secreta123\",\"rol\":\"ADMIN\"}".formatted(email))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rol").value("USER"));
        }

        @Test
        void laRespuestaNoIncluyeLaPassword() throws Exception {
            post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(emailNuevo()))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }

        @Test
        void passwordDebilDa400() throws Exception {
            post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"1234\"}".formatted(emailNuevo()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void emailInvalidoDa400() throws Exception {
            post("/api/auth/registro", "{\"email\":\"no-es-email\",\"password\":\"secreta123\"}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        void loginFallidoNoDiceSiElEmailExiste() throws Exception {
            String r1 = post("/api/auth/login", "{\"email\":\"%s\",\"password\":\"mal12345\"}".formatted(emailNuevo()))
                    .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
            String r2 = post("/api/auth/login", "{\"email\":\"%s\",\"password\":\"mal12345\"}".formatted(ADMIN_EMAIL.toUpperCase()))
                    .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
            // Mismo mensaje en los dos casos
            assertThat(json.readTree(r1).get("detail"))
                    .isEqualTo(json.readTree(r2).get("detail"));
        }

        @Test
        void fuerzaBrutaBloqueaConEl429() throws Exception {
            String email = emailNuevo();
            post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(email));
            for (int i = 0; i < 5; i++) {
                post("/api/auth/login", "{\"email\":\"%s\",\"password\":\"mal12345\"}".formatted(email))
                        .andExpect(status().isUnauthorized());
            }
            // Ni con la contraseña buena: está bloqueado
            post("/api/auth/login", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(email))
                    .andExpect(status().isTooManyRequests());
        }

        @Test
        void refreshRotaYLogoutInvalida() throws Exception {
            String email = emailNuevo();
            post("/api/auth/registro", "{\"email\":\"%s\",\"password\":\"secreta123\"}".formatted(email));
            String r1 = login(email, "secreta123").get("refreshToken").asText();

            String res = post("/api/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(r1))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            String r2 = json.readTree(res).get("refreshToken").asText();

            post("/api/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(r1)).andExpect(status().isUnauthorized());

            post("/api/auth/logout", "{\"refreshToken\":\"%s\"}".formatted(r2)).andExpect(status().isNoContent());
            post("/api/auth/refresh", "{\"refreshToken\":\"%s\"}".formatted(r2)).andExpect(status().isUnauthorized());
        }
    }

    // ---------- Validación, CORS y cabeceras ----------

    @Nested
    class ValidacionYCabeceras {

        @Test
        void productoConPrecioNegativoDa400() throws Exception {
            mvc.perform(jsonPost("/api/productos", PRODUCTO_JSON.replace("19.99", "-5"))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void productoConStockNegativoDa400() throws Exception {
            mvc.perform(jsonPost("/api/productos", PRODUCTO_JSON.replace("\"stock\":5", "\"stock\":-1"))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void productoConTallaAgotadaQueNoExisteDa400() throws Exception {
            mvc.perform(jsonPost("/api/productos", PRODUCTO_JSON.replace("\"agotadas\":[\"L\"]", "\"agotadas\":[\"XXL\"]"))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void catalogoDevuelveLosCamposQueUsaLaWeb() throws Exception {
            mvc.perform(get("/api/productos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].ref").isString())
                    .andExpect(jsonPath("$[0].seccion").isString())
                    .andExpect(jsonPath("$[0].stock").isNumber())
                    .andExpect(jsonPath("$[0].marca").isString())
                    .andExpect(jsonPath("$[0].agotadas").isArray());
        }

        @Test
        void jsonRotoDa400SinFiltrarDetallesInternos() throws Exception {
            post("/api/auth/login", "{esto no es json")
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(not(containsString("Exception"))));
        }

        @Test
        void corsBloqueaOrigenesDesconocidos() throws Exception {
            mvc.perform(options("/api/productos")
                            .header(HttpHeaders.ORIGIN, "https://web-malvada.com")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void corsPermiteElFrontendLocal() throws Exception {
            mvc.perform(options("/api/productos")
                            .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5500"));
        }

        @Test
        void cabecerasDeSeguridadPresentes() throws Exception {
            mvc.perform(get("/api/productos"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));
        }
    }
}
