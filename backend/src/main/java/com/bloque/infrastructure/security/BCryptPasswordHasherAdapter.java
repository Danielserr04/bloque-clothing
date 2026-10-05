package com.bloque.infrastructure.security;

import com.bloque.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de SALIDA: contraseñas con BCrypt (coste 12, lento a propósito para frenar ataques). */
@Component
class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String passwordEnClaro) {
        return encoder.encode(passwordEnClaro);
    }

    @Override
    public boolean coincide(String passwordEnClaro, String hash) {
        return encoder.matches(passwordEnClaro, hash);
    }
}
