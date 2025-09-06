package com.app.dao;

import com.app.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Producto operations
 * TODO: Implement database operations
 */
public interface ProductoDAO {

    /**
     * Save a new producto to the database
     * @param producto the producto to save
     * @return the saved producto with generated ID
     */
    Producto save(Producto producto);
    // TODO: Implement database logic here

    /**
     * Update an existing producto in the database
     * @param producto the producto to update
     * @return the updated producto
     */
    Producto update(Producto producto);
    // TODO: Implement database logic here

    /**
     * Delete a producto from the database
     * @param id the ID of the producto to delete
     */
    void delete(Long id);
    // TODO: Implement database logic here

    /**
     * Find a producto by ID
     * @param id the producto ID
     * @return Optional containing the producto if found
     */
    Optional<Producto> findById(Long id);
    // TODO: Implement database logic here

    /**
     * Find all productos
     * @return List of all productos
     */
    List<Producto> findAll();
    // TODO: Implement database logic here

    /**
     * Find productos by category
     * @param categoria the category to search for
     * @return List of productos in the specified category
     */
    List<Producto> findByCategoria(String categoria);
    // TODO: Implement database logic here

    /**
     * Find productos with low stock
     * @param threshold the stock threshold
     * @return List of productos with stock below threshold
     */
    List<Producto> findLowStock(int threshold);
    // TODO: Implement database logic here

    /**
     * Check if a producto exists with the given name
     * @param nombre the name to check
     * @return true if producto exists, false otherwise
     */
    boolean existsByNombre(String nombre);
    // TODO: Implement database logic here

    /**
     * Update stock for a producto
     * @param id the producto ID
     * @param newStock the new stock amount
     */
    void updateStock(Long id, int newStock);
    // TODO: Implement database logic here
}