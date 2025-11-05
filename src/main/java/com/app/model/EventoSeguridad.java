package com.app.model;

import java.time.LocalDateTime;

public class EventoSeguridad {
    private String id;
    private LocalDateTime fecha;
    private String tipo;
    private String usuarioId;
    private String descripcion;
    private String detalles;

    public static final String TIPO_ACCESO = "ACCESO";
    public static final String TIPO_ROL = "ROL";
    public static final String TIPO_CONFIGURACION = "CONFIGURACION";
    public static final String TIPO_SEGURIDAD = "SEGURIDAD";

    public EventoSeguridad() {
    }

    public EventoSeguridad(LocalDateTime fecha, String tipo, String usuarioId, String descripcion) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}