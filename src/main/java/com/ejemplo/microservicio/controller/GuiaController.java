package com.ejemplo.microservicio.controller;

import com.ejemplo.microservicio.model.Guia;
import com.ejemplo.microservicio.service.EfsStorageService;
import com.ejemplo.microservicio.service.S3StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Endpoints del sistema de guias de despacho.
 * Base de las rutas: /api/guias
 */
@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    private final EfsStorageService efsService;
    private final S3StorageService s3Service;

    public GuiaController(EfsStorageService efsService, S3StorageService s3Service) {
        this.efsService = efsService;
        this.s3Service = s3Service;
    }

    /** CREAR guia: genera PDF y lo guarda en EFS (Criterio 1). POST /api/guias */
    @PostMapping
    public ResponseEntity<?> crearGuia(@RequestBody Guia guia) throws Exception {
        Path rutaEfs = efsService.generarYGuardarEnEfs(guia);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Guia creada y guardada temporalmente en EFS",
                "id", guia.getId(),
                "rutaEfs", rutaEfs.toString()
        ));
    }

    /** SUBIR a S3 (Criterio 2). POST /api/guias/{id}/subir */
    @PostMapping("/{id}/subir")
    public ResponseEntity<?> subirGuia(@PathVariable String id, @RequestBody Guia guia) {
        guia.setId(id);
        Path rutaEfs = efsService.rutaEnEfs(guia);
        String key = s3Service.subirAS3(guia, rutaEfs);
        efsService.eliminarDeEfs(rutaEfs);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guia subida a S3 correctamente",
                "rutaS3", key
        ));
    }

    /** DESCARGAR con validacion de permisos (Criterio 4).
        GET /api/guias/{id}/descargar?periodo=&transportista= */
    @GetMapping("/{id}/descargar")
    public ResponseEntity<?> descargarGuia(@PathVariable String id,
                                           @RequestParam String periodo,
                                           @RequestParam String transportista,
                                           @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        if (rol == null || !(rol.equals("ADMIN") || rol.equals("TRANSPORTISTA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tiene permisos para descargar esta guia"));
        }
        byte[] contenido = s3Service.descargarDeS3(periodo, transportista, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    /** MODIFICAR/ACTUALIZAR (Criterio 3). PUT /api/guias/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarGuia(@PathVariable String id, @RequestBody Guia guia) throws Exception {
        guia.setId(id);
        Path rutaEfs = efsService.generarYGuardarEnEfs(guia);
        String key = s3Service.actualizarEnS3(guia, rutaEfs);
        efsService.eliminarDeEfs(rutaEfs);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Guia actualizada correctamente en S3",
                "rutaS3", key
        ));
    }

    /** ELIMINAR. DELETE /api/guias/{id}?periodo=&transportista= */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarGuia(@PathVariable String id,
                                          @RequestParam String periodo,
                                          @RequestParam String transportista) {
        s3Service.eliminarDeS3(periodo, transportista, id);
        return ResponseEntity.ok(Map.of("mensaje", "Guia eliminada de S3"));
    }

    /** CONSULTAR historial (Criterio 5). GET /api/guias?periodo=&transportista= */
    @GetMapping
    public ResponseEntity<?> consultarGuias(@RequestParam String periodo,
                                            @RequestParam String transportista) {
        List<String> guias = s3Service.consultarHistorial(periodo, transportista);
        return ResponseEntity.ok(Map.of(
                "transportista", transportista,
                "periodo", periodo,
                "guias", guias
        ));
    }
}
