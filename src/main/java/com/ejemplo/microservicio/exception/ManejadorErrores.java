package com.ejemplo.microservicio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Map;

/**
 * Captura errores comunes y devuelve respuestas JSON entendibles.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<?> guiaNoEncontrada(NoSuchKeyException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "La guia solicitada no existe en S3"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> errorGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrio un error: " + ex.getMessage()));
    }
}
