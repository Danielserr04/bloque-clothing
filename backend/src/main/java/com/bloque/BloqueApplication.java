package com.bloque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Punto de entrada de la API de Bloque. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BloqueApplication {
    public static void main(String[] args) {
        SpringApplication.run(BloqueApplication.class, args);
    }
}
