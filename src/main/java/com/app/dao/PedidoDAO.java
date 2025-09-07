package com.app.dao;

import com.app.model.Pedido;
import com.app.model.Cliente;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Pedido operations
 * TODO: Implement database operations
 */
public interface PedidoDAO {

    /**
     * Save a new pedido to the database
     * @param pedido the pedido to save
     * @return the saved pedido with generated ID
     */
    Pedido save(Pedido pedido);
    // TODO: Implement database logic here

    /**
     * Update an existing pedido in the database
     * @param pedido the pedido to update
     * @return the updated pedido
     */
    Pedido update(Pedido pedido);
    // TODO: Implement database logic here

    /**
     * Delete a pedido from the database
     * @param id the ID of the pedido to delete
     */
    void delete(Long id);
    // TODO: Implement database logic here

    /**
     * Find a pedido by ID
     * @param id the pedido ID
     * @return Optional containing the pedido if found
     */
    Optional<Pedido> findById(Long id);
    // TODO: Implement database logic here

    /**
     * Find all pedidos
     * @return List of all pedidos
     */
    List<Pedido> findAll();
    // TODO: Implement database logic here

    /**
     * Find pedidos by cliente
     * @param cliente the cliente
     * @return List of pedidos for the specified cliente
     */
    List<Pedido> findByCliente(Cliente cliente);
    // TODO: Implement database logic here

    /**
     * Find pedidos by status
     * @param estado the status to search for
     * @return List of pedidos with the specified status
     */
    List<Pedido> findByEstado(String estado);
    // TODO: Implement database logic here

    /**
     * Find pedidos by date range
     * @param startDate the start date
     * @param endDate the end date
     * @return List of pedidos within the date range
     */
    List<Pedido> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    // TODO: Implement database logic here

    /**
     * Find pending pedidos
     * @return List of pedidos with PENDIENTE status
     */
    List<Pedido> findPendingOrders();
    // TODO: Implement database logic here

    /**
     * Update pedido status
     * @param id the pedido ID
     * @param newEstado the new status
     */
    void updateStatus(Long id, String newEstado);
    // TODO: Implement database logic here

    /**
     * Count pedidos by status
     * @param estado the status to count
     * @return number of pedidos with the specified status
     */
    long countByEstado(String estado);
    // TODO: Implement database logic here
}