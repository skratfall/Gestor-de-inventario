package com.app.service;

import com.app.dao.SupabaseDatabaseConnection;
import com.app.security.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfiguracionService {

    private static ConfiguracionService instance;
    private final SessionManager sessionManager;
    private Map<String, String> configCache;

    private ConfiguracionService() {
        this.sessionManager = SessionManager.getInstance();
        this.configCache = new HashMap<>();
        loadConfiguration();
    }

    public static ConfiguracionService getInstance() {
        if (instance == null) {
            synchronized (ConfiguracionService.class) {
                if (instance == null) {
                    instance = new ConfiguracionService();
                }
            }
        }
        return instance;
    }

    private void loadConfiguration() {
        if (!sessionManager.isAdmin()) {
            return;
        }

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT clave, valor FROM configuracion_sistema";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            configCache.clear();
            while (rs.next()) {
                configCache.put(rs.getString("clave"), rs.getString("valor"));
            }
        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    public String getConfigValue(String key) {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo administradores pueden acceder a la configuración");
        }

        return configCache.getOrDefault(key, null);
    }

    public String getConfigValue(String key, String defaultValue) {
        if (!sessionManager.isAdmin()) {
            return defaultValue;
        }

        return configCache.getOrDefault(key, defaultValue);
    }

    public boolean setConfigValue(String key, String value) {
        if (!sessionManager.hasPermission("configuracion", "update")) {
            throw new SecurityException("No tiene permiso para modificar la configuración");
        }

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            // Primero intentamos actualizar
            String updateSql = "UPDATE configuracion_sistema SET valor = ?, updated_at = now(), " +
                             "updated_by = ?::uuid WHERE clave = ?";

            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, value);
            updateStmt.setString(2, sessionManager.getCurrentUser().getId());
            updateStmt.setString(3, key);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected == 0) {
                // Si no se actualizó ninguna fila, intentamos insertar
                String insertSql = "INSERT INTO configuracion_sistema " +
                                 "(clave, valor, tipo, descripcion, categoria, editable_por_usuario, updated_by) " +
                                 "VALUES (?, ?, 'string', 'Configuración automática', 'sistema', true, ?::uuid)";

                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, key);
                insertStmt.setString(2, value);
                insertStmt.setString(3, sessionManager.getCurrentUser().getId());

                rowsAffected = insertStmt.executeUpdate();
            }

            if (rowsAffected > 0) {
                configCache.put(key, value);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error updating configuration: " + e.getMessage());
            return false;
        }
    }

    public boolean testConnection() {
        if (!sessionManager.hasPermission("configuracion", "read")) {
            throw new SecurityException("No tiene permiso para probar la conexión");
        }

        try {
            String url = getConfigValue("db.url");
            String apiKey = getConfigValue("db.apikey");

            // Verificar que los valores no estén vacíos
            if (url == null || url.trim().isEmpty() || 
                apiKey == null || apiKey.trim().isEmpty()) {
                return false;
            }

            // Intentar crear una conexión de prueba
            SupabaseDatabaseConnection testConnection = SupabaseDatabaseConnection.createInstance(url, apiKey);
            Connection conn = testConnection.getConnection();
            
            // Verificar la conexión con una consulta simple
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                
                // Si llegamos aquí, la conexión fue exitosa
                setConfigValue("sync.ultima", LocalDateTime.now().toString());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error testing connection: " + e.getMessage());
            return false;
        }
    }

    public List<ConfigEntry> getAllConfig() {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo administradores pueden ver la configuración");
        }

        List<ConfigEntry> entries = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM configuracion_sistema ORDER BY categoria, clave";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ConfigEntry entry = new ConfigEntry();
                entry.id = rs.getString("id");
                entry.clave = rs.getString("clave");
                entry.valor = rs.getString("valor");
                entry.tipo = rs.getString("tipo");
                entry.descripcion = rs.getString("descripcion");
                entry.categoria = rs.getString("categoria");
                entry.editablePorUsuario = rs.getBoolean("editable_por_usuario");

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    entry.updatedAt = updatedAt.toLocalDateTime();
                }

                entries.add(entry);
            }
        } catch (Exception e) {
            System.err.println("Error fetching configuration: " + e.getMessage());
        }

        return entries;
    }

    public List<ConfigEntry> getConfigByCategory(String category) {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo administradores pueden ver la configuración");
        }

        List<ConfigEntry> entries = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM configuracion_sistema WHERE categoria = ? ORDER BY clave";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ConfigEntry entry = new ConfigEntry();
                entry.id = rs.getString("id");
                entry.clave = rs.getString("clave");
                entry.valor = rs.getString("valor");
                entry.tipo = rs.getString("tipo");
                entry.descripcion = rs.getString("descripcion");
                entry.categoria = rs.getString("categoria");
                entry.editablePorUsuario = rs.getBoolean("editable_por_usuario");

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    entry.updatedAt = updatedAt.toLocalDateTime();
                }

                entries.add(entry);
            }
        } catch (Exception e) {
            System.err.println("Error fetching configuration by category: " + e.getMessage());
        }

        return entries;
    }

    public boolean createConfigEntry(String clave, String valor, String tipo,
                                      String descripcion, String categoria, boolean editablePorUsuario) {
        if (!sessionManager.hasPermission("configuracion", "update")) {
            throw new SecurityException("No tiene permiso para crear configuraciones");
        }

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO configuracion_sistema " +
                         "(clave, valor, tipo, descripcion, categoria, editable_por_usuario, updated_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?::uuid)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, clave);
            stmt.setString(2, valor);
            stmt.setString(3, tipo);
            stmt.setString(4, descripcion);
            stmt.setString(5, categoria);
            stmt.setBoolean(6, editablePorUsuario);
            stmt.setString(7, sessionManager.getCurrentUser().getId());

            stmt.executeUpdate();
            configCache.put(clave, valor);
            return true;

        } catch (Exception e) {
            System.err.println("Error creating configuration entry: " + e.getMessage());
            return false;
        }
    }

    public void reloadConfiguration() {
        loadConfiguration();
    }

    public static class ConfigEntry {
        public String id;
        public String clave;
        public String valor;
        public String tipo;
        public String descripcion;
        public String categoria;
        public boolean editablePorUsuario;
        public LocalDateTime updatedAt;
    }
}
