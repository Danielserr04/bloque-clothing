package com.bloque.application.port.out;

import com.bloque.domain.model.Usuario;

/** Puerto de SALIDA: genera el access token (JWT) de un usuario. */
public interface AccessTokenPort {

    String generar(Usuario usuario);

    /** Segundos que dura el access token. */
    long duracionSegundos();
}
