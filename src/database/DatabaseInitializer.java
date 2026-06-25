package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            createTablesIfNotExist(conn);
            upgradeExistingTables(conn);
        }
    }

    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "password VARCHAR(100) NOT NULL"
                    + ")");

            // 2. Questions
            stmt.execute("CREATE TABLE IF NOT EXISTS questions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "topic VARCHAR(50) NOT NULL, "
                    + "question TEXT NOT NULL, "
                    + "option1 VARCHAR(100) NOT NULL, "
                    + "option2 VARCHAR(100) NOT NULL, "
                    + "option3 VARCHAR(100) NOT NULL, "
                    + "option4 VARCHAR(100) NOT NULL, "
                    + "correct_answer VARCHAR(100) NOT NULL"
                    + ")");

            // Create index separately for SQLite
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_questions_topic ON questions(topic)");

            // 3. Results
            stmt.execute("CREATE TABLE IF NOT EXISTS results ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "topic VARCHAR(50) NOT NULL, "
                    + "score INT NOT NULL, "
                    + "test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "mode VARCHAR(30) DEFAULT 'Practice', "
                    + "correct_count INT DEFAULT 0, "
                    + "total_count INT DEFAULT 0, "
                    + "difficulty VARCHAR(20) DEFAULT 'Mixed'"
                    + ")");

            // 4. Bookmarks
            stmt.execute("CREATE TABLE IF NOT EXISTS bookmarks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "question_id INT NOT NULL, "
                    + "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE, "
                    + "UNIQUE (username, question_id)"
                    + ")");

            // 5. Favorite Domains
            stmt.execute("CREATE TABLE IF NOT EXISTS favorite_domains ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "domain_name VARCHAR(100) NOT NULL, "
                    + "UNIQUE (username, domain_name)"
                    + ")");

            // 6. Achievements
            stmt.execute("CREATE TABLE IF NOT EXISTS user_achievements ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "achievement_name VARCHAR(100) NOT NULL, "
                    + "earned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE (username, achievement_name)"
                    + ")");
        }
    }

    private static void upgradeExistingTables(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();

        // Check and update users columns
        addColumnIfNotExists(conn, meta, "users", "xp", "INT NOT NULL DEFAULT 0");
        addColumnIfNotExists(conn, meta, "users", "level", "INT NOT NULL DEFAULT 1");
        addColumnIfNotExists(conn, meta, "users", "streak", "INT NOT NULL DEFAULT 0");
        addColumnIfNotExists(conn, meta, "users", "last_active_date", "DATE DEFAULT NULL");

        // Check and update questions columns
        addColumnIfNotExists(conn, meta, "questions", "difficulty", "VARCHAR(20) NOT NULL DEFAULT 'Beginner'");
        addColumnIfNotExists(conn, meta, "questions", "explanation", "TEXT DEFAULT NULL");
        addColumnIfNotExists(conn, meta, "questions", "topic_name", "VARCHAR(100) DEFAULT NULL");

        // Check and update results columns
        addColumnIfNotExists(conn, meta, "results", "mode", "VARCHAR(30) DEFAULT 'Practice'");
        addColumnIfNotExists(conn, meta, "results", "correct_count", "INT DEFAULT 0");
        addColumnIfNotExists(conn, meta, "results", "total_count", "INT DEFAULT 0");
        addColumnIfNotExists(conn, meta, "results", "difficulty", "VARCHAR(20) DEFAULT 'Mixed'");
    }

    private static void addColumnIfNotExists(Connection conn, DatabaseMetaData meta, 
                                             String tableName, String columnName, String columnDefinition) throws SQLException {
        boolean exists = false;
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                exists = true;
            }
        }
        
        if (!exists) {
            try (ResultSet rs = meta.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
                if (rs.next()) {
                    exists = true;
                }
            }
        }

        if (!exists) {
            try (Statement stmt = conn.createStatement()) {
                String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
                stmt.execute(sql);
                System.out.println("Executed migration: " + sql);
            } catch (SQLException e) {
                // If it already exists and throws exception, ignore.
                System.err.println("Notice (ignored): Column " + columnName + " on " + tableName + " migration message: " + e.getMessage());
            }
        }
    }
}
