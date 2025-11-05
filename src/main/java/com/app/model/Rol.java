package com.app.model;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;

public class Rol {
    private String id;
    private String nombre;
    private String descripcion;
    private int nivelAcceso;
    private JsonObject permisos;
    private LocalDateTime createdAt;

    public static final String ADMIN = "ADMIN";
    public static final String VENDEDOR = "VENDEDOR";
    public static final String USUARIO = "USUARIO";

    public Rol() {
    }

    public Rol(String id, String nombre, String descripcion, int nivelAcceso, JsonObject permisos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivelAcceso = nivelAcceso;
        this.permisos = permisos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public JsonObject getPermisos() {
        return permisos;
    }

    public void setPermisos(JsonObject permisos) {
        this.permisos = permisos;
    }

    public int getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(int nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return ADMIN.equals(nombre);
    }

    public boolean isVendedor() {
        return VENDEDOR.equals(nombre);
    }

    public boolean isUsuario() {
        return USUARIO.equals(nombre);
    }

    @Override
    public String toString() {
        return "Rol{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
