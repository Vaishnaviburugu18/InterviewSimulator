package service;

import database.DBConnection;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    public User registerUser(String username, String email, String password)
            throws ServiceException, SQLException {
        String u = safeTrim(username);
        String e = safeTrim(email);
        String p = password == null ? "" : password;

        validateRegistration(u, e, p);

        String passwordHash = sha256Hex(p);

        String checkSql = "SELECT id FROM users WHERE username = ? OR email = ?";
        String insertSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, u);
            checkStmt.setString(2, e);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new ValidationException("Username or email already exists.");
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, u);
                insertStmt.setString(2, e);
                insertStmt.setString(3, passwordHash);

                insertStmt.executeUpdate();

                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    int id = -1;
                    if (keys.next()) {
                        id = keys.getInt(1);
                    }
                    return new User(id, u, e);
                }
            }
        }
    }

    public User login(String usernameOrEmail, String password)
            throws ServiceException, SQLException {
        String idOrEmail = safeTrim(usernameOrEmail);
        String p = password == null ? "" : password;

        if (idOrEmail.isEmpty() || p.isBlank()) {
            throw new ValidationException("Username/email and password are required.");
        }

        String sql = "SELECT id, username, email, password FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idOrEmail);
            stmt.setString(2, idOrEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthenticationException("Invalid username/email or password.");
                }

                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String storedHash = rs.getString("password");

                String providedHash = sha256Hex(p);
                if (storedHash == null || !storedHash.equalsIgnoreCase(providedHash)) {
                    throw new AuthenticationException("Invalid username/email or password.");
                }

                return new User(id, username, email);
            }
        }
    }

    private void validateRegistration(String username, String email, String password) throws ValidationException {
        if (username.isEmpty() || username.length() > 50) {
            throw new ValidationException("Username is required (max 50 chars).");
        }
        if (email.isEmpty() || email.length() > 100) {
            throw new ValidationException("Email is required (max 100 chars).");
        }
        if (password.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters.");
        }
    }

    private static String safeTrim(String v) {
        return v == null ? "" : v.trim();
    }

    private static String sha256Hex(String input) throws ServiceException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("SHA-256 not supported on this system.", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

