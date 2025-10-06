package com.app.service;

import com.app.dao.SupabaseDatabaseConnection;
import com.app.security.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncService {

    private static SyncService instance;
    private final SessionManager sessionManager;
    private final Gson gson;
    private String syncEndpointUrl;
    private boolean autoSyncEnabled;
    private int syncIntervalMinutes;

    private SyncService() {
        this.sessionManager = SessionManager.getInstance();
        this.gson = new Gson();
        loadConfiguration();
    }

    public static SyncService getInstance() {
        if (instance == null) {
            synchronized (SyncService.class) {
                if (instance == null) {
                    instance = new SyncService();
                }
            }
        }
        return instance;
    }

    private void loadConfiguration() {
        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT clave, valor FROM configuracion_sistema WHERE categoria = 'SINCRONIZACION'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String clave = rs.getString("clave");
                String valor = rs.getString("valor");

                switch (clave) {
                    case "sync.endpoint_url":
                        this.syncEndpointUrl = valor;
                        break;
                    case "sync.auto_enabled":
                        this.autoSyncEnabled = Boolean.parseBoolean(valor);
                        break;
                    case "sync.interval_minutes":
                        this.syncIntervalMinutes = Integer.parseInt(valor);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading sync configuration: " + e.getMessage());
            this.syncEndpointUrl = "";
            this.autoSyncEnabled = false;
            this.syncIntervalMinutes = 30;
        }
    }

    public SyncResult syncDataToCloud(List<String> tables) {
        if (!sessionManager.hasPermission("sincronizacion", "execute")) {
            return new SyncResult(false, "No tiene permiso para ejecutar sincronización", 0);
        }

        if (syncEndpointUrl == null || syncEndpointUrl.isEmpty()) {
            return new SyncResult(false, "URL de sincronización no configurada", 0);
        }

        String syncLogId = createSyncLog("ENVIO", "Múltiples tablas", "INICIADO");
        LocalDateTime startTime = LocalDateTime.now();
        int totalRecords = 0;
        StringBuilder message = new StringBuilder();

        try {
            for (String table : tables) {
                int recordCount = exportTableData(table);
                totalRecords += recordCount;
                message.append(table).append(": ").append(recordCount).append(" registros; ");
            }

            updateSyncLog(syncLogId, "EXITOSO", message.toString(), totalRecords, startTime);
            return new SyncResult(true, "Sincronización completada exitosamente", totalRecords);

        } catch (Exception e) {
            updateSyncLog(syncLogId, "FALLIDO", "Error: " + e.getMessage(), totalRecords, startTime);
            return new SyncResult(false, "Error en sincronización: " + e.getMessage(), totalRecords);
        }
    }

    public SyncResult syncDataFromCloud(List<String> tables) {
        if (!sessionManager.hasPermission("sincronizacion", "execute")) {
            return new SyncResult(false, "No tiene permiso para ejecutar sincronización", 0);
        }

        if (syncEndpointUrl == null || syncEndpointUrl.isEmpty()) {
            return new SyncResult(false, "URL de sincronización no configurada", 0);
        }

        String syncLogId = createSyncLog("RECEPCION", "Múltiples tablas", "INICIADO");
        LocalDateTime startTime = LocalDateTime.now();
        int totalRecords = 0;
        StringBuilder message = new StringBuilder();

        try {
            for (String table : tables) {
                int recordCount = importTableData(table);
                totalRecords += recordCount;
                message.append(table).append(": ").append(recordCount).append(" registros; ");
            }

            updateSyncLog(syncLogId, "EXITOSO", message.toString(), totalRecords, startTime);
            return new SyncResult(true, "Sincronización completada exitosamente", totalRecords);

        } catch (Exception e) {
            updateSyncLog(syncLogId, "FALLIDO", "Error: " + e.getMessage(), totalRecords, startTime);
            return new SyncResult(false, "Error en sincronización: " + e.getMessage(), totalRecords);
        }
    }

    private int exportTableData(String tableName) throws Exception {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + tableName;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    record.put(columnName, value);
                }
                records.add(record);
            }
        }

        if (!records.isEmpty()) {
            sendDataToEndpoint(tableName, records);
        }

        return records.size();
    }

    private int importTableData(String tableName) throws Exception {
        List<Map<String, Object>> records = fetchDataFromEndpoint(tableName);

        if (records.isEmpty()) {
            return 0;
        }

        return records.size();
    }

    private void sendDataToEndpoint(String tableName, List<Map<String, Object>> records) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(syncEndpointUrl + "/sync/upload");

            JsonObject payload = new JsonObject();
            payload.addProperty("table", tableName);
            payload.addProperty("timestamp", LocalDateTime.now().toString());
            payload.add("records", gson.toJsonTree(records));

            request.setEntity(new StringEntity(gson.toJson(payload)));
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception("HTTP error: " + statusCode);
                }
            }
        }
    }

    private List<Map<String, Object>> fetchDataFromEndpoint(String tableName) throws Exception {
        List<Map<String, Object>> records = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(syncEndpointUrl + "/sync/download?table=" + tableName);
            request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception("HTTP error: " + statusCode);
                }
            }
        }

        return records;
    }

    private String createSyncLog(String tipoOperacion, String tablaAfectada, String estado) {
        String logId = null;
        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO sincronizacion_log (tipo_operacion, tabla_afectada, estado, usuario_id, fecha_inicio) " +
                         "VALUES (?, ?, ?, ?::uuid, now()) RETURNING id";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tipoOperacion);
            stmt.setString(2, tablaAfectada);
            stmt.setString(3, estado);
            stmt.setString(4, sessionManager.getCurrentUser().getId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                logId = rs.getString("id");
            }
        } catch (Exception e) {
            System.err.println("Error creating sync log: " + e.getMessage());
        }
        return logId;
    }

    private void updateSyncLog(String logId, String estado, String mensaje, int registros, LocalDateTime startTime) {
        if (logId == null) return;

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "UPDATE sincronizacion_log SET estado = ?, mensaje = ?, " +
                         "registros_procesados = ?, fecha_fin = now() WHERE id = ?::uuid";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, estado);
            stmt.setString(2, mensaje);
            stmt.setInt(3, registros);
            stmt.setString(4, logId);

            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating sync log: " + e.getMessage());
        }
    }

    public List<SyncLogEntry> getSyncHistory(int limit) {
        List<SyncLogEntry> logs = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM sincronizacion_log ORDER BY created_at DESC LIMIT ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SyncLogEntry entry = new SyncLogEntry();
                entry.id = rs.getString("id");
                entry.tipoOperacion = rs.getString("tipo_operacion");
                entry.tablaAfectada = rs.getString("tabla_afectada");
                entry.registrosProcesados = rs.getInt("registros_procesados");
                entry.estado = rs.getString("estado");
                entry.mensaje = rs.getString("mensaje");

                Timestamp fechaInicio = rs.getTimestamp("fecha_inicio");
                if (fechaInicio != null) {
                    entry.fechaInicio = fechaInicio.toLocalDateTime();
                }

                Timestamp fechaFin = rs.getTimestamp("fecha_fin");
                if (fechaFin != null) {
                    entry.fechaFin = fechaFin.toLocalDateTime();
                }

                logs.add(entry);
            }
        } catch (Exception e) {
            System.err.println("Error fetching sync history: " + e.getMessage());
        }

        return logs;
    }

    public boolean isAutoSyncEnabled() {
        return autoSyncEnabled;
    }

    public int getSyncIntervalMinutes() {
        return syncIntervalMinutes;
    }

    public String getSyncEndpointUrl() {
        return syncEndpointUrl;
    }

    public static class SyncResult {
        public final boolean success;
        public final String message;
        public final int recordsProcessed;

        public SyncResult(boolean success, String message, int recordsProcessed) {
            this.success = success;
            this.message = message;
            this.recordsProcessed = recordsProcessed;
        }
    }

    public static class SyncLogEntry {
        public String id;
        public String tipoOperacion;
        public String tablaAfectada;
        public int registrosProcesados;
        public String estado;
        public String mensaje;
        public LocalDateTime fechaInicio;
        public LocalDateTime fechaFin;
    }
}
