package com.bloque.application.service;

import com.bloque.application.port.in.AutenticacionUseCase;
import com.bloque.application.port.out.AccessTokenPort;
import com.bloque.application.port.out.PasswordHasherPort;
import com.bloque.application.port.out.RefreshTokenRepositoryPort;
import com.bloque.application.port.out.UsuarioRepositoryPort;
import com.bloque.domain.exception.CredencialesInvalidasException;
import com.bloque.domain.exception.DatosInvalidosException;
import com.bloque.domain.exception.DemasiadosIntentosException;
import com.bloque.domain.exception.EmailYaRegistradoException;
import com.bloque.domain.model.RefreshToken;
import com.bloque.domain.model.Tokens;
import com.bloque.domain.model.Usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Implementación de los casos de uso de autenticación.
 * Solo depende de puertos (interfaces), así se puede testear sin Spring ni BD.
 */
public class AutenticacionService implements AutenticacionUseCase {

    /** Mismo mensaje para "no existe" y "contraseña mal": así no se puede averiguar qué emails existen. */
    static final String MENSAJE_CREDENCIALES = "Email o contraseña incorrectos";

    // Mínimo 8 caracteres, al menos una letra y un número. Máximo 72 (límite de BCrypt).
    private static final Pattern PASSWORD_SEGURA = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$");

    private final UsuarioRepositoryPort usuarios;
    private final RefreshTokenRepositoryPort refreshTokens;
    private final PasswordHasherPort hasher;
    private final AccessTokenPort accessTokens;
    private final LimitadorIntentosLogin limitador;
    private final Clock clock;
    private final Duration duracionRefresh;
    private final SecureRandom random = new SecureRandom();
    // Hash "de mentira" para comparar cuando el email no existe y tardar lo mismo (evita ataques de tiempo).
    private final String hashFalso;

    public AutenticacionService(UsuarioRepositoryPort usuarios, RefreshTokenRepositoryPort refreshTokens,
                                PasswordHasherPort hasher, AccessTokenPort accessTokens,
                                LimitadorIntentosLogin limitador, Clock clock, Duration duracionRefresh) {
        this.usuarios = usuarios;
        this.refreshTokens = refreshTokens;
        this.hasher = hasher;
        this.accessTokens = accessTokens;
        this.limitador = limitador;
        this.clock = clock;
        this.duracionRefresh = duracionRefresh;
        this.hashFalso = hasher.hash("password-falsa-para-igualar-tiempos");
    }

    @Override
    public Usuario registrar(String email, String password, String nombre) {
        String emailNormalizado = normalizar(email);
        if (password == null || !PASSWORD_SEGURA.matcher(password).matches()) {
            throw new DatosInvalidosException(
                    "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número");
        }
        if (usuarios.existeEmail(emailNormalizado)) {
            throw new EmailYaRegistradoException("Ese email ya está registrado");
        }
        // El rol SIEMPRE es USER: nadie puede auto-registrarse como ADMIN.
        return usuarios.guardar(Usuario.nuevoCliente(emailNormalizado, hasher.hash(password), nombre));
    }

    @Override
    public Tokens login(String email, String password) {
        String emailNormalizado = normalizar(email);
        if (limitador.estaBloqueado(emailNormalizado)) {
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Prueba más tarde");
        }

        Usuario usuario = usuarios.buscarPorEmail(emailNormalizado).orElse(null);
        String hash = usuario != null ? usuario.passwordHash() : hashFalso;
        boolean passwordOk = password != null && hasher.coincide(password, hash);

        if (usuario == null || !passwordOk) {
            limitador.registrarFallo(emailNormalizado);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
        }
        limitador.registrarExito(emailNormalizado);
        return emitirTokens(usuario);
    }

    @Override
    public Tokens refrescar(String refreshToken) {
        RefreshToken guardado = buscarRefreshValido(refreshToken);
        // Rotación: el refresh token usado se borra y se emite uno nuevo.
        // Si otra petición lo borró antes (uso doble), no se emite nada.
        if (!refreshTokens.borrarPorHash(guardado.tokenHash())) {
            throw new CredencialesInvalidasException("Refresh token no válido");
        }
        Usuario usuario = usuarios.buscarPorId(guardado.usuarioId())
                .orElseThrow(() -> new CredencialesInvalidasException("Refresh token no válido"));
        return emitirTokens(usuario);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokens.borrarPorHash(sha256(refreshToken));
        }
    }

    private RefreshToken buscarRefreshValido(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CredencialesInvalidasException("Refresh token no válido");
        }
        RefreshToken guardado = refreshTokens.buscarPorHash(sha256(refreshToken))
                .orElseThrow(() -> new CredencialesInvalidasException("Refresh token no válido"));
        if (guardado.caducado(clock.instant())) {
            refreshTokens.borrarPorHash(guardado.tokenHash());
            throw new CredencialesInvalidasException("Refresh token caducado");
        }
        return guardado;
    }

    private Tokens emitirTokens(Usuario usuario) {
        // Refresh token: 32 bytes aleatorios (no es un JWT). En BD solo va su hash SHA-256.
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.guardar(new RefreshToken(sha256(refresh), usuario.id(), clock.instant().plus(duracionRefresh)));

        return new Tokens(accessTokens.generar(usuario), refresh, accessTokens.duracionSegundos());
    }

    private static String normalizar(String email) {
        if (email == null || email.isBlank()) {
            throw new DatosInvalidosException("El email es obligatorio");
        }
        return email.trim().toLowerCase();
    }

    static String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
