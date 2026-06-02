package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {}

    // Prefer setting these via environment variables:
    //   DB_URL, DB_USER, DB_PASSWORD
    // Fallbacks are for local/dev usage only.
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/interview_simulator?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Vaishnavi@18";

    public static Connection getConnection() throws SQLException {
        String url = getenvOrDefault("DB_URL", DEFAULT_URL);
        String user = getenvOrDefault("DB_USER", DEFAULT_USER);
        String password = getenvOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);

        // MySQL 8+ driver is usually auto-registered, but explicit load is harmless for older setups.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver may already be available via ServiceLoader.
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String getenvOrDefault(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }
}

