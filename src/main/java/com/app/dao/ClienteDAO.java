package com.app.dao;

import com.app.model.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Cliente operations
 * TODO: Implement database operations
 */
public interface ClienteDAO {

    /**
     * Save a new cliente to the database
     * @param cliente the cliente to save
     * @return the saved cliente with generated ID
     */
    Cliente save(Cliente cliente);
    // TODO: Implement database logic here

    /**
     * Update an existing cliente in the database
     * @param cliente the cliente to update
     * @return the updated cliente
     */
    Cliente update(Cliente cliente);
    // TODO: Implement database logic here

    /**
     * Delete a cliente from the database
     * @param id the ID of the cliente to delete
     */
    void delete(Long id);
    // TODO: Implement database logic here

    /**
     * Find a cliente by ID
     * @param id the cliente ID
     * @return Optional containing the cliente if found
     */
    Optional<Cliente> findById(Long id);
    // TODO: Implement database logic here

    /**
     * Find all clientes
     * @return List of all clientes
     */
    List<Cliente> findAll();
    // TODO: Implement database logic here

    /**
     * Find a cliente by email
     * @param email the email address
     * @return Optional containing the cliente if found
     */
    Optional<Cliente> findByEmail(String email);
    // TODO: Implement database logic here

    /**
     * Find clientes by name (partial match)
     * @param nombre the name to search for
     * @return List of clientes matching the name
     */
    List<Cliente> findByNombre(String nombre);
    // TODO: Implement database logic here

    /**
     * Find a cliente by phone number
     * @param telefono the phone number
     * @return Optional containing the cliente if found
     */
    Optional<Cliente> findByTelefono(String telefono);
    // TODO: Implement database logic here

    /**
     * Check if a cliente exists with the given email
     * @param email the email to check
     * @return true if cliente exists, false otherwise
     */
    boolean existsByEmail(String email);
    // TODO: Implement database logic here

    /**
     * Check if a cliente exists with the given phone
     * @param telefono the phone to check
     * @return true if cliente exists, false otherwise
     */
    boolean existsByTelefono(String telefono);
    // TODO: Implement database logic here
}