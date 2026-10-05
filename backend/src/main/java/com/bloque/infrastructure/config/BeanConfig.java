package com.bloque.infrastructure.config;

import com.bloque.application.port.in.AutenticacionUseCase;
import com.bloque.application.port.in.ProductoUseCase;
import com.bloque.application.port.out.*;
import com.bloque.application.service.AutenticacionService;
import com.bloque.application.service.LimitadorIntentosLogin;
import com.bloque.application.service.ProductoService;
import com.bloque.infrastructure.security.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

/**
 * "Enchufa" las piezas: crea los servicios de aplicación pasándoles los adaptadores.
 * Así la capa de aplicación no necesita ninguna anotación de Spring.
 */
@Configuration
class BeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    LimitadorIntentosLogin limitadorIntentosLogin(Clock clock,
            @Value("${bloque.login.max-intentos:5}") int maxIntentos,
            @Value("${bloque.login.bloqueo-minutos:15}") long bloqueoMinutos) {
        return new LimitadorIntentosLogin(maxIntentos, Duration.ofMinutes(bloqueoMinutos), clock);
    }

    @Bean
    AutenticacionUseCase autenticacionUseCase(UsuarioRepositoryPort usuarios, RefreshTokenRepositoryPort refresh,
            PasswordHasherPort hasher, AccessTokenPort accessTokens, LimitadorIntentosLogin limitador,
            Clock clock, JwtProperties props) {
        return new AutenticacionService(usuarios, refresh, hasher, accessTokens, limitador, clock,
                Duration.ofDays(props.refreshTokenDias()));
    }

    @Bean
    ProductoUseCase productoUseCase(ProductoRepositoryPort productos) {
        return new ProductoService(productos);
    }
}
