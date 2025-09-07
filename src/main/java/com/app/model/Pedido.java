package com.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido entity class representing an order in the system
 */
public class Pedido {
    private Long id;
    private LocalDateTime fecha;
    private Cliente cliente;
    private String estado;
    private List<Producto> listaProductos;

    // Order states constants
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_PROCESANDO = "PROCESANDO";
    public static final String ESTADO_ENVIADO = "ENVIADO";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    // Default constructor
    public Pedido() {
        this.listaProductos = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.estado = ESTADO_PENDIENTE;
    }

    // Constructor with parameters
    public Pedido(Cliente cliente, List<Producto> listaProductos) {
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.estado = ESTADO_PENDIENTE;
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
    }

    // Constructor with parameters including estado
    public Pedido(Cliente cliente, String estado, List<Producto> listaProductos) {
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.estado = estado != null ? estado : ESTADO_PENDIENTE;
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
    }

    // Constructor with all parameters including ID
    public Pedido(Long id, LocalDateTime fecha, Cliente cliente, String estado, List<Producto> listaProductos) {
        this.id = id;
        this.fecha = fecha;
        this.cliente = cliente;
        this.estado = estado;
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<Producto> getListaProductos() {
        return new ArrayList<>(listaProductos);
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos != null ? new ArrayList<>(listaProductos) : new ArrayList<>();
    }

    // Business methods
    public void addProducto(Producto producto) {
        if (producto != null) {
            this.listaProductos.add(producto);
        }
    }

    public void removeProducto(Producto producto) {
        if (producto != null) {
            this.listaProductos.remove(producto);
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

    public boolean isPendiente() {
        return ESTADO_PENDIENTE.equals(estado);
    }

    public boolean isProcesando() {
        return ESTADO_PROCESANDO.equals(estado);
    }

    public boolean isEnviado() {
        return ESTADO_ENVIADO.equals(estado);
    }

    public boolean isEntregado() {
        return ESTADO_ENTREGADO.equals(estado);
    }

    public boolean isCancelado() {
        return ESTADO_CANCELADO.equals(estado);
    }

    public void marcarComoProcesando() {
        this.estado = ESTADO_PROCESANDO;
    }

    public void marcarComoEnviado() {
        this.estado = ESTADO_ENVIADO;
    }

    public void marcarComoEntregado() {
        this.estado = ESTADO_ENTREGADO;
    }

    public void cancelar() {
        this.estado = ESTADO_CANCELADO;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", cliente=" + cliente +
                ", estado='" + estado + '\'' +
                ", listaProductos=" + listaProductos.size() + " items" +
                ", total=" + calculateTotal() +
                '}';
    }
}