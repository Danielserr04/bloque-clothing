package com.bloque.infrastructure.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Endpoint protegido de ejemplo: devuelve quién eres según tu token. */
@RestController
@RequestMapping("/api/usuarios")
class UsuarioController {

    @GetMapping("/me")
    Map<String, Object> yo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "id", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "roles", jwt.getClaimAsStringList("roles") == null ? List.of() : jwt.getClaimAsStringList("roles"));
    }
}
