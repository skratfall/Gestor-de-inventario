package com.app.service;

import com.app.dao.SupabaseDatabaseConnection;
import com.app.security.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  // Agregado para logging

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfiguracionService {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionService.class);  // Logger SLF4J

    private static volatile ConfiguracionService instance;  // Volatile para thread-safety
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

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clave, valor FROM configuracion_sistema")) {

            configCache.clear();
            while (rs.next()) {
                configCache.put(rs.getString("clave"), rs.getString("valor"));
            }
            logger.debug("Configuración cargada: {} entradas", configCache.size());
        } catch (Exception e) {
            logger.error("Error loading configuration: {}", e.getMessage(), e);
        }
    }

    public String getConfigValue(String key) {
        if (!sessionManager.isAdmin()) {
            logger.warn("Usuario no administrador intentando acceder a configuración: {}", key);
            return null;  // Devuelve null en lugar de lanzar excepción
        }

        return configCache.getOrDefault(key, null);
    }

    public String getConfigValue(String key, String defaultValue) {
        if (!sessionManager.isAdmin()) {
            logger.warn("Usuario no administrador intentando acceder a configuración: {}", key);
            return defaultValue;  // Devuelve valor por defecto
        }

        return configCache.getOrDefault(key, defaultValue);
    }

    public boolean setConfigValue(String key, String value) {
        if (!sessionManager.hasPermission("configuracion", "update")) {
            throw new SecurityException("No tiene permiso para modificar la configuración");
        }

        try {
            // Primero intentamos actualizar
            String updateSql = "UPDATE configuracion_sistema SET valor = ?, updated_at = now(), " +
                             "updated_by = ?::uuid WHERE clave = ?";

            try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                updateStmt.setString(1, value);
                // Manejo de UUID: Convierte string a UUID si es válido
                if (sessionManager.getCurrentUser() != null) {
                    try {
                        updateStmt.setObject(2, java.util.UUID.fromString(sessionManager.getCurrentUser().getId()));
                    } catch (IllegalArgumentException e) {
                        logger.warn("ID de usuario no es UUID válido: {}", sessionManager.getCurrentUser().getId());
                        updateStmt.setObject(2, null);
                    }
                } else {
                    updateStmt.setObject(2, null);
                }
                updateStmt.setString(3, key);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    // Si no se actualizó ninguna fila, intentamos insertar
                    String insertSql = "INSERT INTO configuracion_sistema " +
                                     "(clave, valor, tipo, descripcion, categoria, editable_por_usuario, updated_by) " +
                                     "VALUES (?, ?, 'string', 'Configuración automática', 'sistema', true, ?::uuid)";

                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, key);
                        insertStmt.setString(2, value);
                        if (sessionManager.getCurrentUser() != null) {
                            try {
                                insertStmt.setObject(3, java.util.UUID.fromString(sessionManager.getCurrentUser().getId()));
                            } catch (IllegalArgumentException e) {
                                logger.warn("ID de usuario no es UUID válido: {}", sessionManager.getCurrentUser().getId());
                                insertStmt.setObject(3, null);
                            }
                        } else {
                            insertStmt.setObject(3, null);
                        }

                        rowsAffected = insertStmt.executeUpdate();
                    }
                }

                if (rowsAffected > 0) {
                    configCache.put(key, value);
                    logger.debug("Configuración actualizada: {} = {}", key, value);
                    return true;
                }

                return false;
            }

        } catch (Exception e) {
            logger.error("Error updating configuration: {}", e.getMessage(), e);
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
            // Intentar crear una conexión de prueba usando DriverManager en try-with-resources
            try (Connection conn = java.sql.DriverManager.getConnection(url, "postgres", apiKey);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");

                // Si llegamos aquí, la conexión fue exitosa
                setConfigValue("sync.ultima", LocalDateTime.now().toString());
                logger.info("Conexión a Supabase probada exitosamente");
                return true;
            }
        } catch (Exception e) {
            logger.error("Error testing connection: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<ConfigEntry> getAllConfig() {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo administradores pueden ver la configuración");
        }

        List<ConfigEntry> entries = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM configuracion_sistema ORDER BY categoria, clave")) {

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
            logger.debug("Obtenidas {} entradas de configuración", entries.size());
        } catch (Exception e) {
            logger.error("Error fetching configuration: {}", e.getMessage(), e);
        }

        return entries;
    }

    public List<ConfigEntry> getConfigByCategory(String category) {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo administradores pueden ver la configuración");
        }

        List<ConfigEntry> entries = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM configuracion_sistema WHERE categoria = ? ORDER BY clave")) {

            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
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
            }
            logger.debug("Obtenidas {} entradas para categoría {}", entries.size(), category);
        } catch (Exception e) {
            logger.error("Error fetching configuration by category: {}", e.getMessage(), e);
        }

        return entries;
    }

    public boolean createConfigEntry(String clave, String valor, String tipo,
                                      String descripcion, String categoria, boolean editablePorUsuario) {
        if (!sessionManager.hasPermission("configuracion", "update")) {
            throw new SecurityException("No tiene permiso para crear configuraciones");
        }

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO configuracion_sistema " +
                     "(clave, valor, tipo, descripcion, categoria, editable_por_usuario, updated_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?::uuid)")) {

            stmt.setString(1, clave);
            stmt.setString(2, valor);
            stmt.setString(3, tipo);
            stmt.setString(4, descripcion);
            stmt.setString(5, categoria);
            stmt.setBoolean(6, editablePorUsuario);
            if (sessionManager.getCurrentUser() != null) {
                try {
                    stmt.setObject(7, java.util.UUID.fromString(sessionManager.getCurrentUser().getId()));
                } catch (IllegalArgumentException e) {
                    logger.warn("ID de usuario no es UUID válido: {}", sessionManager.getCurrentUser().getId());
                    stmt.setObject(7, null);
                }
            } else {
                stmt.setObject(7, null);
            }

            stmt.executeUpdate();
            configCache.put(clave, valor);
            logger.debug("Entrada de configuración creada: {}", clave);
            return true;

        } catch (Exception e) {
            logger.error("Error creating configuration entry: {}", e.getMessage(), e);
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
