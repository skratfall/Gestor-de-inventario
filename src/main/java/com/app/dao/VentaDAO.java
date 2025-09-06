package com.app.dao;

import com.app.model.Venta;
import com.app.model.Cliente;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Venta operations
 * TODO: Implement database operations
 */
public interface VentaDAO {

    /**
     * Save a new venta to the database
     * @param venta the venta to save
     * @return the saved venta with generated ID
     */
    Venta save(Venta venta);
    // TODO: Implement database logic here

    /**
     * Update an existing venta in the database
     * @param venta the venta to update
     * @return the updated venta
     */
    Venta update(Venta venta);
    // TODO: Implement database logic here

    /**
     * Delete a venta from the database
     * @param id the ID of the venta to delete
     */
    void delete(Long id);
    // TODO: Implement database logic here

    /**
     * Find a venta by ID
     * @param id the venta ID
     * @return Optional containing the venta if found
     */
    Optional<Venta> findById(Long id);
    // TODO: Implement database logic here

    /**
     * Find all ventas
     * @return List of all ventas
     */
    List<Venta> findAll();
    // TODO: Implement database logic here

    /**
     * Find ventas by cliente
     * @param cliente the cliente
     * @return List of ventas for the specified cliente
     */
    List<Venta> findByCliente(Cliente cliente);
    // TODO: Implement database logic here

    /**
     * Find ventas by date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of ventas within the date range
     */
    List<Venta> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    // TODO: Implement database logic here

    /**
     * Find ventas by minimum total amount
     * @param minTotal the minimum total amount
     * @return List of ventas with total >= minTotal
     */
    List<Venta> findByMinTotal(double minTotal);
    // TODO: Implement database logic here

    /**
     * Calculate total sales for a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return total sales amount
     */
    double calculateTotalSales(LocalDateTime startDate, LocalDateTime endDate);
    // TODO: Implement database logic here

    /**
     * Get sales count for a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return number of sales
     */
    long getSalesCount(LocalDateTime startDate, LocalDateTime endDate);
    // TODO: Implement database logic here
}