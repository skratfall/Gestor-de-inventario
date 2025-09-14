package com.app.model;

/**
 * Class representing an item in the shopping cart
 */
public class CarritoItem {
    private Producto producto;
    private int cantidad;
    
    public CarritoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    
    public void incrementarCantidad() {
        this.cantidad++;
    }
    
    public void decrementarCantidad() {
        if (this.cantidad > 1) {
            this.cantidad--;
        }
    }
    
    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CarritoItem that = (CarritoItem) obj;
        return producto.getId().equals(that.producto.getId());
    }
    
    @Override
    public int hashCode() {
        return producto.getId().hashCode();
    }
}