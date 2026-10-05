package com.bloque.application;

import com.bloque.application.port.out.*;
import com.bloque.application.service.AutenticacionService;
import com.bloque.application.service.LimitadorIntentosLogin;
import com.bloque.domain.exception.*;
import com.bloque.domain.model.RefreshToken;
import com.bloque.domain.model.Rol;
import com.bloque.domain.model.Tokens;
import com.bloque.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests UNITARIOS de la capa de aplicación: sin Spring ni base de datos.
 * Es la gracia de la arquitectura hexagonal: los puertos se sustituyen por versiones falsas en memoria.
 */
class AutenticacionServiceTest {

    private RelojFalso reloj;
    private UsuariosEnMemoria usuarios;
    private RefreshEnMemoria refresh;
    private AutenticacionService auth;

    @BeforeEach
    void setUp() {
        reloj = new RelojFalso(Instant.parse("2026-01-01T10:00:00Z"));
        usuarios = new UsuariosEnMemoria();
        refresh = new RefreshEnMemoria();
        auth = new AutenticacionService(usuarios, refresh, new HasherFalso(), new AccessTokenFalso(),
                new LimitadorIntentosLogin(3, Duration.ofMinutes(15), reloj), reloj, Duration.ofDays(7));
    }

    @Test
    void registroGuardaHashYRolUser() {
        Usuario u = auth.registrar("  Ana@Mail.com ", "secreta123", "Ana");

        assertThat(u.email()).isEqualTo("ana@mail.com");
        assertThat(u.rol()).isEqualTo(Rol.USER);
        assertThat(u.passwordHash()).isNotEqualTo("secreta123");
    }

    @Test
    void registroRechazaPasswordDebil() {
        assertThatThrownBy(() -> auth.registrar("a@a.com", "corta1", "A")).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> auth.registrar("a@a.com", "sinnumeros", "A")).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> auth.registrar("a@a.com", "12345678", "A")).isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void registroRechazaEmailRepetidoAunqueCambienMayusculas() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        assertThatThrownBy(() -> auth.registrar("ANA@mail.com", "secreta123", "Ana"))
                .isInstanceOf(EmailYaRegistradoException.class);
    }

    @Test
    void loginCorrectoDevuelveTokens() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        Tokens t = auth.login("ana@mail.com", "secreta123");

        assertThat(t.accessToken()).startsWith("access-");
        assertThat(t.refreshToken()).hasSizeGreaterThan(40);
    }

    @Test
    void mismoErrorSiEmailNoExisteOPasswordMal() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");

        Throwable noExiste = catchThrowable(() -> auth.login("nadie@mail.com", "secreta123"));
        Throwable passMal = catchThrowable(() -> auth.login("ana@mail.com", "otra1234"));

        assertThat(noExiste).isInstanceOf(CredencialesInvalidasException.class);
        assertThat(passMal).isInstanceOf(CredencialesInvalidasException.class);
        assertThat(noExiste.getMessage()).isEqualTo(passMal.getMessage());
    }

    @Test
    void bloqueaTrasVariosFallosYSeDesbloqueaConElTiempo() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> auth.login("ana@mail.com", "mal12345")).isInstanceOf(CredencialesInvalidasException.class);
        }
        // Bloqueado incluso con la contraseña buena
        assertThatThrownBy(() -> auth.login("ana@mail.com", "secreta123")).isInstanceOf(DemasiadosIntentosException.class);

        reloj.avanzar(Duration.ofMinutes(16));
        assertThat(auth.login("ana@mail.com", "secreta123")).isNotNull();
    }

    @Test
    void refreshRotaElToken() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        Tokens t1 = auth.login("ana@mail.com", "secreta123");

        Tokens t2 = auth.refrescar(t1.refreshToken());

        assertThat(t2.refreshToken()).isNotEqualTo(t1.refreshToken());
        // El refresh viejo ya no sirve (evita reutilizar un token robado)
        assertThatThrownBy(() -> auth.refrescar(t1.refreshToken())).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void refreshCaducadoNoSirve() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        Tokens t = auth.login("ana@mail.com", "secreta123");

        reloj.avanzar(Duration.ofDays(8));

        assertThatThrownBy(() -> auth.refrescar(t.refreshToken())).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void logoutInvalidaElRefresh() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        Tokens t = auth.login("ana@mail.com", "secreta123");

        auth.logout(t.refreshToken());

        assertThatThrownBy(() -> auth.refrescar(t.refreshToken())).isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void elRefreshNoSeGuardaEnClaro() {
        auth.registrar("ana@mail.com", "secreta123", "Ana");
        Tokens t = auth.login("ana@mail.com", "secreta123");

        assertThat(refresh.datos.keySet()).doesNotContain(t.refreshToken());
    }

    // ---------- Implementaciones falsas de los puertos ----------

    static class RelojFalso extends Clock {
        private Instant ahora;

        RelojFalso(Instant ahora) { this.ahora = ahora; }

        void avanzar(Duration d) { ahora = ahora.plus(d); }

        @Override public Instant instant() { return ahora; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    static class UsuariosEnMemoria implements UsuarioRepositoryPort {
        final Map<Long, Usuario> datos = new HashMap<>();
        long siguienteId = 1;

        public Optional<Usuario> buscarPorEmail(String email) {
            return datos.values().stream().filter(u -> u.email().equals(email)).findFirst();
        }
        public Optional<Usuario> buscarPorId(Long id) { return Optional.ofNullable(datos.get(id)); }
        public boolean existeEmail(String email) { return buscarPorEmail(email).isPresent(); }
        public Usuario guardar(Usuario u) {
            Usuario conId = new Usuario(siguienteId++, u.email(), u.passwordHash(), u.nombre(), u.rol());
            datos.put(conId.id(), conId);
            return conId;
        }
    }

    static class RefreshEnMemoria implements RefreshTokenRepositoryPort {
        final Map<String, RefreshToken> datos = new HashMap<>();

        public void guardar(RefreshToken t) { datos.put(t.tokenHash(), t); }
        public Optional<RefreshToken> buscarPorHash(String h) { return Optional.ofNullable(datos.get(h)); }
        public boolean borrarPorHash(String h) { return datos.remove(h) != null; }
    }

    static class HasherFalso implements PasswordHasherPort {
        public String hash(String p) { return "hash:" + new StringBuilder(p).reverse(); }
        public boolean coincide(String p, String h) { return hash(p).equals(h); }
    }

    static class AccessTokenFalso implements AccessTokenPort {
        public String generar(Usuario u) { return "access-" + u.id(); }
        public long duracionSegundos() { return 900; }
    }
}
