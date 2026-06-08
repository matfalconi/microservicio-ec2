package com.ejemplo.microservicio.model;

/**
 * Representa una Guia de Despacho.
 * Estos son los datos que viajan en las peticiones REST.
 */
public class Guia {

    private String id;              // identificador unico, ej: "guia123"
    private String transportista;   // ej: "transportistaX" (organiza carpetas en S3)
    private String periodo;         // ej: "20211" (organiza carpetas en S3)
    private String origen;
    private String destino;
    private String descripcionCarga;
    private String fechaEmision;

    public Guia() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
