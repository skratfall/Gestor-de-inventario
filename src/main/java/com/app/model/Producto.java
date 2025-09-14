package com.app.model;

import javafx.beans.property.*;

/**
 * Producto entity class representing a product in the system
 */
public class Producto {

    private final LongProperty id;
    private final StringProperty nombre;
    private final StringProperty categoria;
    private final DoubleProperty precio;
    private final IntegerProperty stock;

    // Constructor por defecto
    public Producto() {
        this.id = new SimpleLongProperty();
        this.nombre = new SimpleStringProperty();
        this.categoria = new SimpleStringProperty();
        this.precio = new SimpleDoubleProperty();
        this.stock = new SimpleIntegerProperty();
    }

    // Constructor con parámetros (sin ID)
    public Producto(String nombre, String categoria, double precio, int stock) {
        this();
        this.nombre.set(nombre);
        this.categoria.set(categoria);
        this.precio.set(precio);
        this.stock.set(stock);
    }

    // Constructor con parámetros (incluyendo ID)
    public Producto(Long id, String nombre, String categoria, double precio, int stock) {
        this(nombre, categoria, precio, stock);
        this.id.set(id);
    }

    // 🔹 Getters y Setters estilo JavaFX Properties
    public Long getId() {
        return id.get();
    }
    public void setId(Long id) {
        this.id.set(id);
    }
    public LongProperty idProperty() {
        return id;
    }

    public String getNombre() {
        return nombre.get();
    }
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }
    public StringProperty nombreProperty() {
        return nombre;
    }

    public String getCategoria() {
        return categoria.get();
    }
    public void setCategoria(String categoria) {
        this.categoria.set(categoria);
    }
    public StringProperty categoriaProperty() {
        return categoria;
    }

    public double getPrecio() {
        return precio.get();
    }
    public void setPrecio(double precio) {
        this.precio.set(precio);
    }
    public DoubleProperty precioProperty() {
        return precio;
    }

    public int getStock() {
        return stock.get();
    }
    public void setStock(int stock) {
        this.stock.set(stock);
    }
    public IntegerProperty stockProperty() {
        return stock;
    }

    // 🔹 Status dinámico (para la columna "Estado")
    public String getStatus() {
        return getStock() > 0 ? "Disponible" : "Agotado";
    }

    // Métodos de negocio
    public boolean isAvailable() {
        return getStock() > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= getStock()) {
            setStock(getStock() - quantity);
        } else {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + getStock() + ", Solicitado: " + quantity);
        }
    }

    public void increaseStock(int quantity) {
        setStock(getStock() + quantity);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", categoria='" + getCategoria() + '\'' +
                ", precio=" + getPrecio() +
                ", stock=" + getStock() +
                ", status=" + getStatus() +
                '}';
    }
}
