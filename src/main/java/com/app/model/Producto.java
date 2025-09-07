package com.app.model;

/**
 * Producto entity class representing a product in the system
 */
public class Producto {
    private Long id;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;

    // Default constructor
    public Producto() {
    }

    // Constructor with parameters
    public Producto(String nombre, String categoria, double precio, int stock) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    // Constructor with all parameters including ID
    public Producto(Long id, String nombre, String categoria, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Business methods
    public boolean isAvailable() {
        return stock > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= stock) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock. Available: " + stock + ", Requested: " + quantity);
        }
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}