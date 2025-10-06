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
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(".env")) {
            if (input == null) {
                System.err.println("Unable to find .env file, using environment variables");
                loadFromEnvironment();
                return;
            }

            props.load(input);
            String supabaseUrl = props.getProperty("VITE_SUPABASE_URL");

            if (supabaseUrl != null) {
                String projectRef = extractProjectRef(supabaseUrl);
                this.dbUrl = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres";
                this.dbUser = "postgres." + projectRef;
                this.dbPassword = System.getenv("SUPABASE_DB_PASSWORD");

                if (this.dbPassword == null || this.dbPassword.isEmpty()) {
                    System.err.println("Warning: SUPABASE_DB_PASSWORD not set in environment");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            loadFromEnvironment();
        }
    }

    private void loadFromEnvironment() {
        String supabaseUrl = System.getenv("VITE_SUPABASE_URL");
        if (supabaseUrl != null) {
            String projectRef = extractProjectRef(supabaseUrl);
            this.dbUrl = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres";
            this.dbUser = "postgres." + projectRef;
            this.dbPassword = System.getenv("SUPABASE_DB_PASSWORD");
        }
    }

    private String extractProjectRef(String supabaseUrl) {
        String cleaned = supabaseUrl.replace("https://", "").replace("http://", "");
        int dotIndex = cleaned.indexOf('.');
        if (dotIndex > 0) {
            return cleaned.substring(0, dotIndex);
        }
        return cleaned;
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
                System.out.println("Supabase database connection established.");
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
                throw new SQLException("PostgreSQL JDBC Driver not found", e);
            } catch (SQLException e) {
                System.err.println("Failed to establish Supabase database connection: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Supabase database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing Supabase database connection: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Supabase database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
