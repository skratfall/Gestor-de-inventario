package com.app.dao;

import com.app.model.Rol;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolDAO {

    private Connection getConnection() throws SQLException {
        return SupabaseDatabaseConnection.getInstance().getConnection();
    }

    public Optional<Rol> findById(String id) {
        String sql = "SELECT * FROM roles WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRol(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding rol by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<Rol> findByNombre(String nombre) {
        String sql = "SELECT * FROM roles WHERE nombre = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRol(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding rol by nombre: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public List<Rol> findAll() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY nombre";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(mapResultSetToRol(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding all roles: " + e.getMessage());
            e.printStackTrace();
        }

        return roles;
    }

    public Rol save(Rol rol) {
        String sql = "INSERT INTO roles (nombre, descripcion, permisos) VALUES (?, ?, ?::jsonb) RETURNING id, created_at";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setString(3, rol.getPermisos() != null ? rol.getPermisos().toString() : "{}");

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                rol.setId(rs.getString("id"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    rol.setCreatedAt(createdAt.toLocalDateTime());
                }
            }

            return rol;

        } catch (SQLException e) {
            System.err.println("Error saving rol: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save rol", e);
        }
    }

    public Rol update(Rol rol) {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ?, permisos = ?::jsonb WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setString(3, rol.getPermisos() != null ? rol.getPermisos().toString() : "{}");
            stmt.setString(4, rol.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Rol not found with id: " + rol.getId());
            }

            return rol;

        } catch (SQLException e) {
            System.err.println("Error updating rol: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update rol", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM roles WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Rol not found with id: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error deleting rol: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete rol", e);
        }
    }

    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getString("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));

        String permisosJson = rs.getString("permisos");
        if (permisosJson != null && !permisosJson.isEmpty()) {
            JsonObject permisos = JsonParser.parseString(permisosJson).getAsJsonObject();
            rol.setPermisos(permisos);
        } else {
            rol.setPermisos(new JsonObject());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rol.setCreatedAt(createdAt.toLocalDateTime());
        }

        return rol;
    }
}
