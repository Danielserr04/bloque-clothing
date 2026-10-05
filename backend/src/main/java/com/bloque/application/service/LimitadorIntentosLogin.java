package com.bloque.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección contra fuerza bruta: si un email falla el login {@code maxIntentos}
 * veces seguidas, queda bloqueado durante {@code bloqueo}.
 *
 * Se guarda en memoria: vale para un solo servidor. Si algún día hay varios,
 * habría que moverlo a Redis o a la BD.
 */
public class LimitadorIntentosLogin {

    private record Intentos(int fallos, Instant primerFallo) {}

    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration bloqueo;
    private final Clock clock;

    public LimitadorIntentosLogin(int maxIntentos, Duration bloqueo, Clock clock) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.clock = clock;
    }

    public boolean estaBloqueado(String email) {
        Intentos i = intentos.get(email);
        if (i == null) {
            return false;
        }
        if (ventanaCaducada(i)) {
            intentos.remove(email);
            return false;
        }
        return i.fallos() >= maxIntentos;
    }

    public void registrarFallo(String email) {
        intentos.compute(email, (k, i) -> (i == null || ventanaCaducada(i))
                ? new Intentos(1, clock.instant())
                : new Intentos(i.fallos() + 1, i.primerFallo()));
    }

    public void registrarExito(String email) {
        intentos.remove(email);
    }

    private boolean ventanaCaducada(Intentos i) {
        return clock.instant().isAfter(i.primerFallo().plus(bloqueo));
    }
}
