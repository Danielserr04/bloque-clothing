package com.bloque.application.port.out;

/** Puerto de SALIDA: cifrado de contraseñas (la implementación usa BCrypt). */
public interface PasswordHasherPort {

    String hash(String passwordEnClaro);

    boolean coincide(String passwordEnClaro, String hash);
}
