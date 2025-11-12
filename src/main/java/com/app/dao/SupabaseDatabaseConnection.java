package com.app.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Proveedor de conexiones para Supabase/Postgres.
 * Implementado con HikariCP como pool de conexiones para evitar alcanzar el límite de clientes
 * en el servidor y para manejar conexiones concurrentes de forma segura.
 */
public class SupabaseDatabaseConnection {

    private static SupabaseDatabaseConnection instance;
    private HikariDataSource dataSource;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int maxPoolSize = 10; // valor por defecto
    private boolean poolInitialized = false;
    private boolean fallbackMode = false; // Usar DriverManager si pool falla

    private SupabaseDatabaseConnection() {
        loadProperties();
        initDataSource();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("❌ No se encontró application.properties en resources/");
            }
            props.load(input);

            this.dbUrl = props.getProperty("DB_URL");
            this.dbUser = props.getProperty("DB_USER");
            this.dbPassword = props.getProperty("DB_PASSWORD");

            String poolSize = props.getProperty("DB_POOL_SIZE");
            if (poolSize != null) {
                try {
                    this.maxPoolSize = Integer.parseInt(poolSize);
                } catch (NumberFormatException ignored) {
                }
            }

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new RuntimeException("❌ Faltan propiedades de conexión en application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Error cargando configuración de base de datos", e);
        }
    }

    private void initDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(0); // No pre-create connections; only create when needed
            config.setAutoCommit(true);
            config.setConnectionTimeout(30000); // 30s
            config.setIdleTimeout(600000); // 10min
            config.setMaxLifetime(1800000); // 30min
            config.setLeakDetectionThreshold(60000); // Detect connections not closed after 60s

            System.out.println("✅ Inicializando HikariCP pool con máximo: " + maxPoolSize + " conexiones.");
            this.dataSource = new HikariDataSource(config);
            this.poolInitialized = true;
        } catch (Exception e) {
            System.err.println("⚠️ HikariCP pool inicialización falló: " + e.getMessage());
            System.err.println("⚠️ Usando modo fallback con DriverManager (menos eficiente)");
            this.fallbackMode = true;
            this.poolInitialized = false;
        }
    }

    public static SupabaseDatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (SupabaseDatabaseConnection.class) {
                if (instance == null) {
                    try {
                        instance = new SupabaseDatabaseConnection();
                    } catch (RuntimeException e) {
                        System.err.println("❌ Error inicial al crear SupabaseDatabaseConnection: " + e.getMessage());
                        // Reintentar después de 5 segundos si el pool está saturado
                        if (e.getMessage() != null && e.getMessage().contains("Max client connections")) {
                            System.out.println("⏳ Esperando 5 segundos antes de reintentar...");
                            try {
                                Thread.sleep(5000);
                                instance = new SupabaseDatabaseConnection();
                                System.out.println("✅ Reintento exitoso");
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Interrumpido durante retry", e);
                            }
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Devuelve una conexión del pool (o DriverManager si pool falló).
     * El llamador debe cerrarla (try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        if (poolInitialized && dataSource != null) {
            return dataSource.getConnection();
        } else if (fallbackMode) {
            // Fallback: crear conexión directa con DriverManager
            try {
                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } catch (ClassNotFoundException e) {
                throw new SQLException("❌ Driver PostgreSQL no encontrado", e);
            }
        } else {
            throw new SQLException("DataSource no inicializado");
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error testing DB connection: " + e.getMessage());
            return false;
        }
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("🔒 Pool Hikari cerrado.");
        }
    }
}
