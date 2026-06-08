package com.ejemplo.microservicio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion.
 * Arranca el servidor web con todos los endpoints de guias de despacho.
 */
@SpringBootApplication
public class MicroservicioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroservicioApplication.class, args);
    }
}
