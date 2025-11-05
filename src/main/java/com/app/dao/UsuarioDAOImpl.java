package com.app.dao;

import com.app.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImpl implements UsuarioDAO {

    private Connection getConnection() throws SQLException {
            Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos");
            }
            return conn;
    }

    @Override
    public Usuario save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, email, nombre_completo, rol_id, activo) " +
                     "VALUES (?, ?, ?, ?, ?::uuid, ?) RETURNING id, created_at, updated_at";

            try (Connection conn = getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, usuario.getUsername());
                    stmt.setString(2, usuario.getPasswordHash());
                    stmt.setString(3, usuario.getEmail());
                    stmt.setString(4, usuario.getNombreCompleto());
                    stmt.setString(5, usuario.getRolId());
                    stmt.setBoolean(6, usuario.isActivo());


                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            usuario.setId(rs.getString("id"));
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");


                            if (createdAt != null) {
                                usuario.setCreatedAt(createdAt.toLocalDateTime());
                            }
                            if (updatedAt != null) {
                                usuario.setUpdatedAt(updatedAt.toLocalDateTime());
                            }
                        }
                    }
            }

            return usuario;

        } catch (SQLException e) {
            System.err.println("Error saving usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save usuario", e);
        }
    }

    @Override
    public Usuario update(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, email = ?, nombre_completo = ?, " +
                     "rol_id = ?::uuid, activo = ?, updated_at = now() WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getNombreCompleto());
            stmt.setString(4, usuario.getRolId());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.setString(6, usuario.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Usuario not found with id: " + usuario.getId());
            }

            usuario.setUpdatedAt(LocalDateTime.now());
            return usuario;

        } catch (SQLException e) {
            System.err.println("Error updating usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update usuario", e);
        }
    }

    @Override
    public void delete(Long id) {
        delete(id.toString());
    }

    public void delete(String id) {
        String sql = "DELETE FROM usuarios WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Usuario not found with id: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error deleting usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete usuario", e);
        }
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return findById(id.toString());
    }

    public Optional<Usuario> findById(String id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding usuario by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY username";

            try (Connection conn = getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            usuarios.add(mapResultSetToUsuario(rs));
                        }
                    }
                }


        } catch (SQLException e) {
            System.err.println("Error finding all usuarios: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE username = ?";

            try (Connection conn = getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(mapResultSetToUsuario(rs));
                        }
                    }
                }


        } catch (SQLException e) {
            System.err.println("Error finding usuario by username: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Usuario> findByRol(String rol) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.* FROM usuarios u " +
                     "INNER JOIN roles r ON u.rol_id = r.id " +
                     "WHERE r.nombre = ? ORDER BY u.username";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding usuarios by rol: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS(SELECT 1 FROM usuarios WHERE username = ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(1);
            }

        } catch (SQLException e) {
            System.err.println("Error checking if username exists: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Optional<Usuario> authenticate(String username, String password) {
        return findByUsername(username);
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        updatePassword(id.toString(), newPassword);
    }

    public void updatePassword(String id, String newPasswordHash) {
        String sql = "UPDATE usuarios SET password_hash = ?, updated_at = now() WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Usuario not found with id: " + id);
            }

        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update password", e);
        }
    }

    public void updateLastAccess(String id) {
        String sql = "UPDATE usuarios SET ultimo_acceso = now(), updated_at = now() WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating last access: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public long countByRol(String rol) {
        String sql = "SELECT COUNT(*) FROM usuarios u " +
                     "INNER JOIN roles r ON u.rol_id = r.id " +
                     "WHERE r.nombre = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            System.err.println("Error counting usuarios by rol: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public List<Usuario> findAllWithoutAuth() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY username";

            try (Connection conn = getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            usuarios.add(mapResultSetToUsuario(rs));
                        }
                    }
                }


        } catch (SQLException e) {
            System.err.println("Error finding all usuarios without auth: " + e.getMessage());
            e.printStackTrace();
        }

        return usuarios;
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getString("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setEmail(rs.getString("email"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setRolId(rs.getString("rol_id"));
        usuario.setActivo(rs.getBoolean("activo"));

        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }
        if (createdAt != null) {
            usuario.setCreatedAt(createdAt.toLocalDateTime());
        }
        if (updatedAt != null) {
            usuario.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return usuario;
    }
}
