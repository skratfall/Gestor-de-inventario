package com.app.model;

/**
 * Cliente entity class representing a customer in the system
 */
public class Cliente {
    private Long id;
    private String nombre;
    private String telefono;
    private String email;

    // Default constructor
    public Cliente() {
    }

    // Constructor with parameters
    public Cliente(String nombre, String telefono, String email) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    // Constructor with all parameters including ID
    public Cliente(Long id, String nombre, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Business methods
    public boolean hasValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean hasValidPhone() {
        return telefono != null && !telefono.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}