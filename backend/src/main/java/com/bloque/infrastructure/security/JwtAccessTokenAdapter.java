package com.bloque.infrastructure.security;

import com.bloque.application.port.out.AccessTokenPort;
import com.bloque.domain.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de SALIDA: crea el access token (JWT firmado con HS256).
 * Contenido: quién es (sub = id, email), su rol y cuándo caduca.
 */
@Component
class JwtAccessTokenAdapter implements AccessTokenPort {

    private final JwtEncoder encoder;
    private final JwtProperties props;
    private final Clock clock;

    JwtAccessTokenAdapter(JwtEncoder encoder, JwtProperties props, Clock clock) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
    }

    @Override
    public String generar(Usuario usuario) {
        Instant ahora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(usuario.id()))
                .id(UUID.randomUUID().toString())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(duracionSegundos()))
                .claim("email", usuario.email())
                .claim("roles", List.of(usuario.rol().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long duracionSegundos() {
        return props.accessTokenMinutos() * 60;
    }
}
