package com.app.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SupabaseDatabaseConnection {

    private static SupabaseDatabaseConnection instance;
    private Connection connection;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private SupabaseDatabaseConnection() {
        loadProperties();
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

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new RuntimeException("❌ Faltan propiedades de conexión en application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Error cargando configuración de base de datos", e);
        }
    }

    public static SupabaseDatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (SupabaseDatabaseConnection.class) {
                if (instance == null) {
                    instance = new SupabaseDatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                System.out.println("✅ Conexión establecida con Supabase PostgreSQL.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("❌ Driver PostgreSQL no encontrado", e);
            }
        }
        return connection;
    }

    /**
     * Prueba rápida de conexión: intenta abrir una conexión y la cierra.
     * Devuelve true si la conexión se pudo establecer correctamente.
     */
    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error testing DB connection: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cerrando conexión: " + e.getMessage());
        }
    }
}
