package com.app.model;

/**
 * Usuario entity class representing a system user
 */
public class Usuario {
    private Long id;
    private String username;
    private String password;
    private String rol;

    // Role constants
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_VENDEDOR = "VENDEDOR";
    public static final String ROL_USUARIO = "USUARIO";

    // Default constructor
    public Usuario() {
    }

    // Constructor with parameters
    public Usuario(String username, String password, String rol) {
        this.username = username;
        this.password = password;
        this.rol = rol != null ? rol : ROL_USUARIO;
    }

    // Constructor with all parameters including ID
    public Usuario(Long id, String username, String password, String rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    // Business methods
    public boolean isAdmin() {
        return ROL_ADMIN.equals(rol);
    }

    public boolean isVendedor() {
        return ROL_VENDEDOR.equals(rol);
    }

    public boolean isUsuario() {
        return ROL_USUARIO.equals(rol);
    }

    public boolean hasRole(String role) {
        return role != null && role.equals(this.rol);
    }

    public boolean canManageProducts() {
        return isAdmin() || isVendedor();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    public boolean canViewReports() {
        return isAdmin() || isVendedor();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}