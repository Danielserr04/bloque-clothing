package com.bloque.infrastructure.web;

import com.bloque.application.port.in.AutenticacionUseCase;
import com.bloque.infrastructure.web.dto.AuthDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Adaptador de ENTRADA: endpoints de autenticación. Solo traduce HTTP <-> caso de uso. */
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AutenticacionUseCase auth;

    AuthController(AutenticacionUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponse registro(@Valid @RequestBody RegistroRequest req) {
        return UsuarioResponse.de(auth.registrar(req.email(), req.password(), req.nombre()));
    }

    @PostMapping("/login")
    TokensResponse login(@Valid @RequestBody LoginRequest req) {
        return TokensResponse.de(auth.login(req.email(), req.password()));
    }

    @PostMapping("/refresh")
    TokensResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return TokensResponse.de(auth.refrescar(req.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshRequest req) {
        auth.logout(req.refreshToken());
    }
}
