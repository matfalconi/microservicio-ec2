package com.ejemplo.microservicio.service;

import com.ejemplo.microservicio.model.Guia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja toda la comunicacion con AWS S3 (Criterios 2, 3, 4, 5).
 *
 * Estructura de carpetas en el bucket:
 *   {periodo}/{transportista}/{idGuia}.pdf
 *   ejemplo -> 20211/transportistaX/guia123.pdf
 */
@Service
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private String construirKey(Guia guia) {
        return guia.getPeriodo() + "/" + guia.getTransportista() + "/" + guia.getId() + ".pdf";
    }

    /** CRITERIO 2: sube la guia a S3 en su carpeta organizada. */
    public String subirAS3(Guia guia, Path rutaArchivoLocal) {
        String key = construirKey(guia);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .build();
        s3Client.putObject(request, RequestBody.fromFile(rutaArchivoLocal));
        return key;
    }

    /** CRITERIO 3: actualiza (misma key sobrescribe el archivo anterior). */
    public String actualizarEnS3(Guia guia, Path rutaArchivoLocal) {
        return subirAS3(guia, rutaArchivoLocal);
    }

    /** CRITERIO 4: descarga el contenido del PDF desde S3. */
    public byte[] descargarDeS3(String periodo, String transportista, String idGuia) {
        String key = periodo + "/" + transportista + "/" + idGuia + ".pdf";
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseBytes<GetObjectResponse> objeto = s3Client.getObjectAsBytes(request);
        return objeto.asByteArray();
    }

    /** CRITERIO 5: lista las guias de un transportista en un periodo. */
    public List<String> consultarHistorial(String periodo, String transportista) {
        String prefijo = periodo + "/" + transportista + "/";
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefijo)
                .build();

        ListObjectsV2Response respuesta = s3Client.listObjectsV2(request);
        List<String> guias = new ArrayList<>();
        for (S3Object objeto : respuesta.contents()) {
            guias.add(objeto.key());
        }
        return guias;
    }

    /** Elimina una guia de S3. */
    public void eliminarDeS3(String periodo, String transportista, String idGuia) {
        String key = periodo + "/" + transportista + "/" + idGuia + ".pdf";
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }
}
