package ethioskill.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // Default Configuration (Fallbacks for local development)
    // Update these if your local database credentials change
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ethioskill_db1221?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "password12";
    
    // Load the MySQL Driver once when the application starts
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver not found!");
            System.err.println("Please ensure mysql-connector-java-8.x.jar is in WEB-INF/lib/");
            e.printStackTrace();
        }
    }
    
    /**
     * Establishes a NEW connection to the database.
     * Note: In a web application, never share a static Connection object.
     * Always create a new one and close it when done (using try-with-resources).
     * 
     * @return A valid Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        // 1. Try to get credentials from System Environment Variables (Best Practice for Prod)
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // 2. If Environment Variables are empty, use the Default Fallbacks (Local Dev)
        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_URL;
        }
        if (user == null || user.trim().isEmpty()) {
            user = DEFAULT_USER;
        }
        if (password == null || password.trim().isEmpty()) {
            password = DEFAULT_PASSWORD;
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Database Connection Failed!");
            System.err.println("URL: " + url);
            System.err.println("User: " + user);
            throw e;
        }
    }
    
    /**
     * Utility method to test if the database is reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}