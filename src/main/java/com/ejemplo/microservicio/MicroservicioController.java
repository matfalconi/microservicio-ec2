package com.ejemplo.microservicio;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/microservicio")
public class MicroservicioController {

    // GET http://localhost:8080/microservicio/{id}
    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable Long id) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("metodo", "GET");
        respuesta.put("id", id);
        respuesta.put("mensaje", "Recurso obtenido correctamente");
        return respuesta;
    }

    // POST http://localhost:8080/microservicio
    // Body: { "mensaje": "integración ok" }
    @PostMapping
    public Map<String, Object> crear(@RequestBody Map<String, String> body) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("metodo", "POST");
        respuesta.put("mensajeRecibido", body.get("mensaje"));
        respuesta.put("estado", "creado");
        return respuesta;
    }

    // PUT http://localhost:8080/microservicio?status=OK
    @PutMapping
    public Map<String, Object> actualizar(@RequestParam String status) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("metodo", "PUT");
        respuesta.put("status", status);
        respuesta.put("mensaje", "Recurso actualizado");
        return respuesta;
    }

    // DELETE http://localhost:8080/microservicio
    // Header: Authorization: hola mundo
    @DeleteMapping
    public Map<String, Object> eliminar(@RequestHeader("Authorization") String authorization) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("metodo", "DELETE");
        respuesta.put("authorization", authorization);
        respuesta.put("mensaje", "Recurso eliminado");
        return respuesta;
    }

}
