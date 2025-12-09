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

        String syncLogId = createSyncLog("ENVIO", "Múltiples tablas", "INICIADO");
        LocalDateTime startTime = LocalDateTime.now();
        int totalRecords = 0;
        StringBuilder message = new StringBuilder();

        try {
            for (String table : tables) {
                try {
                    int recordCount = exportAndStoreTableData(table);
                    totalRecords += recordCount;
                    message.append(table).append(": ").append(recordCount).append(" registros; ");
                } catch (Exception e) {
                    message.append(table).append(": ERROR - ").append(e.getMessage()).append("; ");
                }
            }

            updateSyncLog(syncLogId, "EXITOSO", message.toString(), totalRecords, startTime);
            return new SyncResult(true, "Sincronización completada exitosamente. " + totalRecords + " registros enviados.", totalRecords);

        } catch (Exception e) {
            updateSyncLog(syncLogId, "FALLIDO", "Error: " + e.getMessage(), totalRecords, startTime);
            return new SyncResult(false, "Error en sincronización: " + e.getMessage(), totalRecords);
        }
    }

    public SyncResult syncDataFromCloud(List<String> tables) {
        if (!sessionManager.hasPermission("sincronizacion", "execute")) {
            return new SyncResult(false, "No tiene permiso para ejecutar sincronización", 0);
        }

        String syncLogId = createSyncLog("RECEPCION", "Múltiples tablas", "INICIADO");
        LocalDateTime startTime = LocalDateTime.now();
        int totalRecords = 0;
        StringBuilder message = new StringBuilder();

        try {
            for (String table : tables) {
                try {
                    int recordCount = fetchAndApplyTableData(table);
                    totalRecords += recordCount;
                    message.append(table).append(": ").append(recordCount).append(" registros; ");
                } catch (Exception e) {
                    message.append(table).append(": ERROR - ").append(e.getMessage()).append("; ");
                }
            }

            updateSyncLog(syncLogId, "EXITOSO", message.toString(), totalRecords, startTime);
            return new SyncResult(true, "Sincronización completada exitosamente. " + totalRecords + " registros recibidos.", totalRecords);

        } catch (Exception e) {
            updateSyncLog(syncLogId, "FALLIDO", "Error: " + e.getMessage(), totalRecords, startTime);
            return new SyncResult(false, "Error en sincronización: " + e.getMessage(), totalRecords);
        }
    }

    /**
     * Exporta datos de tabla local y los almacena en tabla de sincronización de Supabase
     */
    private int exportAndStoreTableData(String tableName) throws Exception {
        List<Map<String, Object>> records = new ArrayList<>();

        // 1. Obtener datos de tabla local
        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM " + tableName;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
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
        }

        // 2. Guardar en tabla de sincronización
        if (!records.isEmpty()) {
            storeSyncRecords(tableName, records, "ENVIO");
        }

        return records.size();
    }

    /**
     * Obtiene datos de tabla de sincronización y los aplica localmente
     */
    private int fetchAndApplyTableData(String tableName) throws Exception {
        // 1. Obtener datos pendientes de sincronizar desde Supabase
        List<Map<String, Object>> records = retrieveSyncRecords(tableName, "RECEPCION");

        if (records.isEmpty()) {
            return 0;
        }

        // 2. Aplicar cambios localmente
        int appliedCount = applyChangesToLocalDatabase(tableName, records);
        
        // 3. Marcar como sincronizados
        markSyncRecordsAsProcessed(tableName);

        return appliedCount;
    }

    /**
     * Almacena registros en tabla de sincronización_pendiente
     */
    private void storeSyncRecords(String tableName, List<Map<String, Object>> records, String tipoSync) throws Exception {
        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "INSERT INTO sincronizacion_pendiente (tabla_nombre, tipo_sincronizacion, datos, estado) " +
                         "VALUES (?, ?, ?::jsonb, 'PENDIENTE')";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map<String, Object> record : records) {
                    stmt.setString(1, tableName);
                    stmt.setString(2, tipoSync);
                    stmt.setString(3, gson.toJson(record));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }

    /**
     * Obtiene registros pendientes de sincronización
     */
    private List<Map<String, Object>> retrieveSyncRecords(String tableName, String tipoSync) throws Exception {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT datos FROM sincronizacion_pendiente " +
                         "WHERE tabla_nombre = ? AND tipo_sincronizacion = ? AND estado = 'PENDIENTE' " +
                         "ORDER BY fecha_creacion ASC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tableName);
                stmt.setString(2, tipoSync);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String jsonData = rs.getString("datos");
                        Map<String, Object> record = gson.fromJson(jsonData, Map.class);
                        records.add(record);
                    }
                }
            }
        }

        return records;
    }

    /**
     * Aplica cambios a la BD local (UPSERT)
     */
    private int applyChangesToLocalDatabase(String tableName, List<Map<String, Object>> records) throws Exception {
        int appliedCount = 0;

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            for (Map<String, Object> record : records) {
                String sql = buildUpsertQuery(tableName, record);
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    
                    // Bind para INSERT
                    for (Object value : record.values()) {
                        stmt.setObject(paramIndex++, value);
                    }
                    
                    // Bind para UPDATE (sin ID)
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        if (!entry.getKey().equals("id")) {
                            stmt.setObject(paramIndex++, entry.getValue());
                        }
                    }

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) appliedCount++;
                }
            }
        }

        return appliedCount;
    }

    /**
     * Construye query UPSERT dinámicamente
     */
    private String buildUpsertQuery(String tableName, Map<String, Object> record) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder updates = new StringBuilder();

        boolean first = true;
        for (String key : record.keySet()) {
            if (!first) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(key);
            values.append("?");
            
            if (!key.equals("id")) {
                if (!updates.isEmpty()) updates.append(", ");
                updates.append(key).append(" = ?");
            }
            first = false;
        }

        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ") " +
               "ON CONFLICT (id) DO UPDATE SET " + updates;
    }

    /**
     * Marca registros como procesados
     */
    private void markSyncRecordsAsProcessed(String tableName) throws Exception {
        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            String sql = "UPDATE sincronizacion_pendiente SET estado = 'COMPLETADO', " +
                         "fecha_procesamiento = NOW() WHERE tabla_nombre = ? AND estado = 'PENDIENTE'";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tableName);
                stmt.executeUpdate();
            }
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

        // Insertar o actualizar registros en la BD local
        return insertOrUpdateRecords(tableName, records);
    }

    private int insertOrUpdateRecords(String tableName, List<Map<String, Object>> records) throws Exception {
        int insertedCount = 0;

        try (Connection conn = SupabaseDatabaseConnection.getInstance().getConnection()) {
            for (Map<String, Object> record : records) {
                // Construir query INSERT OR UPDATE (UPSERT)
                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();
                StringBuilder updates = new StringBuilder();

                int paramIndex = 1;
                for (String key : record.keySet()) {
                    if (paramIndex > 1) {
                        columns.append(", ");
                        values.append(", ");
                    }
                    columns.append(key);
                    values.append("?");
                    if (!key.equals("id")) { // No actualizar el ID
                        if (!updates.isEmpty()) updates.append(", ");
                        updates.append(key).append(" = ?");
                    }
                    paramIndex++;
                }

                String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")" +
                             " ON CONFLICT (id) DO UPDATE SET " + updates;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    paramIndex = 1;
                    // Bind para INSERT
                    for (Object value : record.values()) {
                        stmt.setObject(paramIndex++, value);
                    }
                    // Bind para UPDATE
                    for (Object value : record.values()) {
                        if (!record.keySet().toArray()[record.keySet().toArray().length - 1].equals("id")) {
                            stmt.setObject(paramIndex++, value);
                        }
                    }

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) insertedCount++;
                }
            }
        }

        return insertedCount;
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
            HttpGet request = new HttpGet(syncEndpointUrl + "/sync/download?table=" + tableName + "&timestamp=" + LocalDateTime.now().toString());
            request.setHeader("Accept", "application/json");
            request.setHeader("Authorization", "Bearer " + sessionManager.getCurrentUser().getId());

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception("HTTP error: " + statusCode + " - " + response.getReasonPhrase());
                }

                // Extraer JSON de la respuesta
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                if (jsonResponse.has("records")) {
                    JsonArray recordsArray = jsonResponse.getAsJsonArray("records");
                    for (int i = 0; i < recordsArray.size(); i++) {
                        Map<String, Object> record = gson.fromJson(recordsArray.get(i), Map.class);
                        records.add(record);
                    }
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
