package service;

import database.DBConnection;
import model.ResultRecord;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class ProfileService {

    // ─── Bookmarks ────────────────────────────────────────────────────────────

    public void addBookmark(String username, int questionId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO bookmarks (username, question_id) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setInt(2, questionId); ps.executeUpdate();
        }
    }

    public void removeBookmark(String username, int questionId) throws SQLException {
        String sql = "DELETE FROM bookmarks WHERE username=? AND question_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setInt(2, questionId); ps.executeUpdate();
        }
    }

    public boolean isBookmarked(String username, int questionId) throws SQLException {
        String sql = "SELECT 1 FROM bookmarks WHERE username=? AND question_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setInt(2, questionId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ─── Favorite Domains ────────────────────────────────────────────────────

    public void addFavoriteDomain(String username, String domain) throws SQLException {
        String sql = "INSERT OR IGNORE INTO favorite_domains (username, domain_name) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, domain); ps.executeUpdate();
        }
    }

    public void removeFavoriteDomain(String username, String domain) throws SQLException {
        String sql = "DELETE FROM favorite_domains WHERE username=? AND domain_name=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, domain); ps.executeUpdate();
        }
    }

    public Set<String> getFavoriteDomains(String username) throws SQLException {
        Set<String> favs = new LinkedHashSet<>();
        String sql = "SELECT domain_name FROM favorite_domains WHERE username=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) favs.add(rs.getString("domain_name"));
            }
        }
        return favs;
    }

    // ─── Resume Keyword Recommendation ───────────────────────────────────────

    public List<String> recommendDomains(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) return Collections.emptyList();
        String lower = resumeText.toLowerCase();
        List<String> recs = new ArrayList<>();

        // keyword → domain mapping
        Map<String, String> kw = new LinkedHashMap<>();
        kw.put("java", "Java"); kw.put("spring", "Java");
        kw.put("python", "Python"); kw.put("django", "Python"); kw.put("flask", "Python");
        kw.put("javascript", "JavaScript"); kw.put("node", "JavaScript");
        kw.put("react", "React"); kw.put("redux", "React");
        kw.put("sql", "SQL"); kw.put("mysql", "SQL"); kw.put("postgresql", "SQL");
        kw.put("database", "Database Management Systems"); kw.put("dbms", "Database Management Systems");
        kw.put("data structure", "Data Structures and Algorithms"); kw.put("algorithm", "Data Structures and Algorithms");
        kw.put("html", "HTML"); kw.put("css", "CSS");
        kw.put("web dev", "Web Development"); kw.put("rest api", "Web Development");
        kw.put("aws", "Cloud Computing"); kw.put("azure", "Cloud Computing"); kw.put("gcp", "Cloud Computing"); kw.put("cloud", "Cloud Computing");
        kw.put("docker", "DevOps"); kw.put("kubernetes", "DevOps"); kw.put("jenkins", "DevOps"); kw.put("devops", "DevOps");
        kw.put("machine learning", "Machine Learning"); kw.put("tensorflow", "Machine Learning"); kw.put("pytorch", "Machine Learning");
        kw.put("data science", "Data Science"); kw.put("pandas", "Data Science"); kw.put("numpy", "Data Science");
        kw.put("security", "Cyber Security"); kw.put("penetration", "Cyber Security");
        kw.put("network", "Computer Networks"); kw.put("tcp", "Computer Networks");
        kw.put("operating system", "Operating Systems"); kw.put("linux", "Operating Systems");
        kw.put("system design", "System Design"); kw.put("microservice", "System Design");
        kw.put("c++", "C++"); kw.put("cpp", "C++");
        kw.put("oop", "Object Oriented Programming"); kw.put("design pattern", "Object Oriented Programming");

        Set<String> seen = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : kw.entrySet()) {
            if (lower.contains(entry.getKey()) && seen.add(entry.getValue())) {
                recs.add(entry.getValue());
            }
        }
        return recs;
    }

    // ─── Achievements ────────────────────────────────────────────────────────

    public void grantAchievement(String username, String achievementName) throws SQLException {
        String sql = "INSERT OR IGNORE INTO user_achievements (username, achievement_name) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, achievementName); ps.executeUpdate();
        }
    }

    public List<String> getAchievements(String username) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT achievement_name FROM user_achievements WHERE username=? ORDER BY earned_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("achievement_name"));
            }
        }
        return list;
    }

    /** Checks milestone achievements and grants them automatically. */
    public void checkAndGrantMilestones(String username, int testCount, int bestScore) throws SQLException {
        if (testCount >= 1) grantAchievement(username, "First Quiz");
        if (testCount >= 10) grantAchievement(username, "Quiz Enthusiast");
        if (testCount >= 50) grantAchievement(username, "Quiz Master");
        if (bestScore >= 80) grantAchievement(username, "High Scorer");
        if (bestScore == 100) grantAchievement(username, "Perfect Score");
    }

    // ─── Password Update ─────────────────────────────────────────────────────

    public void updatePassword(String username, String oldPassword, String newPassword) throws SQLException, ServiceException {
        if (newPassword == null || newPassword.length() < 4) {
            throw new ValidationException("New password must be at least 4 characters.");
        }
        String oldHash = sha256Hex(oldPassword);
        String newHash = sha256Hex(newPassword);

        String verifySql = "SELECT password FROM users WHERE username = ?";
        String updateSql = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
            verifyStmt.setString(1, username);
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    if (!stored.equalsIgnoreCase(oldHash)) {
                        throw new ValidationException("Incorrect old password.");
                    }
                } else {
                    throw new ServiceException("User not found.");
                }
            }
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newHash);
                updateStmt.setString(2, username);
                updateStmt.executeUpdate();
            }
        }
    }

    private static String sha256Hex(String input) throws ServiceException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("SHA-256 not supported on this system.", e);
        }
    }
}
