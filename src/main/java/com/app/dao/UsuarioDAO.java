package com.app.dao;

import com.app.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Usuario operations
 * TODO: Implement database operations
 */
public interface UsuarioDAO {

    /**
     * Save a new usuario to the database
     * @param usuario the usuario to save
     * @return the saved usuario with generated ID
     */
    Usuario save(Usuario usuario);
    // TODO: Implement database logic here

    /**
     * Update an existing usuario in the database
     * @param usuario the usuario to update
     * @return the updated usuario
     */
    Usuario update(Usuario usuario);
    // TODO: Implement database logic here

    /**
     * Delete a usuario from the database
     * @param id the ID of the usuario to delete
     */
    void delete(Long id);
    // TODO: Implement database logic here

    /**
     * Find a usuario by ID
     * @param id the usuario ID
     * @return Optional containing the usuario if found
     */
    Optional<Usuario> findById(Long id);
    // TODO: Implement database logic here

    /**
     * Find all usuarios
     * @return List of all usuarios
     */
    List<Usuario> findAll();
    // TODO: Implement database logic here

    /**
     * Find a usuario by username
     * @param username the username
     * @return Optional containing the usuario if found
     */
    Optional<Usuario> findByUsername(String username);
    // TODO: Implement database logic here

    /**
     * Find usuarios by role
     * @param rol the role to search for
     * @return List of usuarios with the specified role
     */
    List<Usuario> findByRol(String rol);
    // TODO: Implement database logic here

    /**
     * Check if a usuario exists with the given username
     * @param username the username to check
     * @return true if usuario exists, false otherwise
     */
    boolean existsByUsername(String username);
    // TODO: Implement database logic here

    /**
     * Authenticate a usuario with username and password
     * @param username the username
     * @param password the password
     * @return Optional containing the usuario if authentication successful
     */
    Optional<Usuario> authenticate(String username, String password);
    // TODO: Implement database logic here

    /**
     * Update usuario password
     * @param id the usuario ID
     * @param newPassword the new password
     */
    void updatePassword(Long id, String newPassword);
    // TODO: Implement database logic here

    /**
     * Count usuarios by role
     * @param rol the role to count
     * @return number of usuarios with the specified role
     */
    long countByRol(String rol);
    // TODO: Implement database logic here
}