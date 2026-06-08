package com.ejemplo.microservicio.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.ejemplo.microservicio.model.Guia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CRITERIO 1: Genera el PDF de la guia y lo guarda TEMPORALMENTE en el EFS,
 * organizado en subcarpetas por periodo y transportista.
 */
@Service
public class EfsStorageService {

    @Value("${efs.ruta-temporal}")
    private String rutaEfs;

    public Path generarYGuardarEnEfs(Guia guia) throws IOException {
        Path carpeta = Paths.get(rutaEfs, guia.getPeriodo(), guia.getTransportista());
        Files.createDirectories(carpeta);

        Path rutaPdf = carpeta.resolve(guia.getId() + ".pdf");

        try (PdfWriter writer = new PdfWriter(rutaPdf.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document documento = new Document(pdf)) {

            documento.add(new Paragraph("GUIA DE DESPACHO").setBold().setFontSize(18));
            documento.add(new Paragraph("N de Guia: " + guia.getId()));
            documento.add(new Paragraph("Transportista: " + guia.getTransportista()));
            documento.add(new Paragraph("Periodo: " + guia.getPeriodo()));
            documento.add(new Paragraph("Fecha de emision: " + guia.getFechaEmision()));
            documento.add(new Paragraph("Origen: " + guia.getOrigen()));
            documento.add(new Paragraph("Destino: " + guia.getDestino()));
            documento.add(new Paragraph("Descripcion de la carga: " + guia.getDescripcionCarga()));
        }

        return rutaPdf;
    }

    public Path rutaEnEfs(Guia guia) {
        return Paths.get(rutaEfs, guia.getPeriodo(), guia.getTransportista(), guia.getId() + ".pdf");
    }

    public void eliminarDeEfs(Path ruta) {
        File archivo = ruta.toFile();
        if (archivo.exists()) {
            archivo.delete();
        }
    }
}
