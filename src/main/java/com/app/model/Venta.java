package com.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta entity class representing a sale in the system
 */
public class Venta {
    private Long id;
    private LocalDateTime fecha;
    private Cliente cliente;
    private List<Producto> listaProductos;
    private double total;

    // Default constructor
    public Venta() {
        this.listaProductos = new ArrayList<>();
        this.fecha = LocalDateTime.now();
    }

    // Constructor with parameters
    public Venta(Cliente cliente, List<Producto> listaProductos) {
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
        this.total = calculateTotal();
    }

    // Constructor with all parameters including ID
    public Venta(Long id, LocalDateTime fecha, Cliente cliente, List<Producto> listaProductos, double total) {
        this.id = id;
        this.fecha = fecha;
        this.cliente = cliente;
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
        this.total = total;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Producto> getListaProductos() {
        return new ArrayList<>(listaProductos);
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
        this.total = calculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Business methods
    public void addProducto(Producto producto) {
        if (producto != null) {
            this.listaProductos.add(producto);
            this.total = calculateTotal();
        }
    }

    public void removeProducto(Producto producto) {
        if (producto != null && this.listaProductos.remove(producto)) {
            this.total = calculateTotal();
        }
    }

    public double calculateTotal() {
        return listaProductos.stream()
                .mapToDouble(Producto::getPrecio)
                .sum();
    }

    public int getTotalItems() {
        return listaProductos.size();
    }

    public boolean isEmpty() {
        return listaProductos.isEmpty();
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", cliente=" + cliente +
                ", listaProductos=" + listaProductos.size() + " items" +
                ", total=" + total +
                '}';
    }
}