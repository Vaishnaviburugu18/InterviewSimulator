package service;

import database.DBConnection;
import model.ResultRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultService {

    // ─── Save ────────────────────────────────────────────────────────────────

    public void saveResult(String username, String topic, int scorePercent,
                           String mode, int correctCount, int totalCount, String difficulty)
            throws ServiceException, SQLException {
        String u = trim(username); String t = trim(topic);
        if (u.isEmpty() || t.isEmpty()) throw new ValidationException("Username/topic required.");
        int s = Math.max(0, Math.min(100, scorePercent));

        String sql = "INSERT INTO results (username, topic, score, mode, correct_count, total_count, difficulty) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u); ps.setString(2, t); ps.setInt(3, s);
            ps.setString(4, mode != null ? mode : "Practice");
            ps.setInt(5, correctCount); ps.setInt(6, totalCount);
            ps.setString(7, difficulty != null ? difficulty : "Mixed");
            ps.executeUpdate();
        }
        // Award XP and update user stats
        awardXP(username, computeXP(s, totalCount));
    }

    // Legacy overloads for backward compat
    public void saveResult(String username, String topic, int scorePercent)
            throws ServiceException, SQLException {
        saveResult(username, topic, scorePercent, "Practice", 0, 0, "Mixed");
    }

    public void saveResult(String username, String topic, int scorePercent, String mode, int correctCount, int totalCount)
            throws ServiceException, SQLException {
        saveResult(username, topic, scorePercent, mode, correctCount, totalCount, "Mixed");
    }

    // ─── XP & Levels ─────────────────────────────────────────────────────────

    private int computeXP(int scorePercent, int totalQuestions) {
        return (scorePercent / 10) + Math.min(totalQuestions, 20);
    }

    public void awardXP(String username, int xpGained) throws SQLException {
        if (xpGained <= 0) return;
        String sql = "UPDATE users SET xp = xp + ?, level = ((xp + ?) / 100) + 1 WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, xpGained); ps.setInt(2, xpGained); ps.setString(3, username);
            ps.executeUpdate();
        }
    }

    // ─── Query helpers ────────────────────────────────────────────────────────

    public int getTestCount(String username) throws SQLException {
        return queryInt("SELECT COUNT(*) FROM results WHERE username=?", username);
    }

    public int getBestScore(String username) throws SQLException {
        return queryInt("SELECT COALESCE(MAX(score),0) FROM results WHERE username=?", username);
    }

    public int getAverageScore(String username) throws SQLException {
        return queryInt("SELECT COALESCE(AVG(score),0) FROM results WHERE username=?", username);
    }

    public int getUserXP(String username) throws SQLException {
        return queryInt("SELECT COALESCE(xp,0) FROM users WHERE username=?", username);
    }

    public int getUserLevel(String username) throws SQLException {
        int lvl = queryInt("SELECT COALESCE(level,1) FROM users WHERE username=?", username);
        return Math.max(1, lvl);
    }

    public int getUserStreak(String username) throws SQLException {
        return queryInt("SELECT COALESCE(streak,0) FROM users WHERE username=?", username);
    }

    private int queryInt(String sql, String param) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ─── History ─────────────────────────────────────────────────────────────

    public List<ResultRecord> getPreviousResults(String username)
            throws ServiceException, SQLException {
        String u = trim(username);
        if (u.isEmpty()) throw new ValidationException("Username required.");
        String sql = "SELECT id, username, topic, score, test_date, "
                   + "COALESCE(mode,'Practice') AS mode, "
                   + "COALESCE(correct_count,0) AS correct_count, "
                   + "COALESCE(total_count,0) AS total_count, "
                   + "COALESCE(difficulty,'Mixed') AS difficulty "
                   + "FROM results WHERE username=? ORDER BY test_date DESC LIMIT 100";
        List<ResultRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new ResultRecord(
                        rs.getInt("id"), rs.getString("username"),
                        rs.getString("topic"), rs.getInt("score"),
                        rs.getTimestamp("test_date"), rs.getString("mode"),
                        rs.getInt("correct_count"), rs.getInt("total_count"),
                        rs.getString("difficulty")));
                }
            }
        }
        return records;
    }

    // ─── Analytics ───────────────────────────────────────────────────────────

    /** Returns a map of domain -> average score for the user. */
    public Map<String, Integer> getDomainAverages(String username) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT topic, ROUND(AVG(score)) AS avg_score FROM results "
                   + "WHERE username=? GROUP BY topic ORDER BY avg_score DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("topic"), rs.getInt("avg_score"));
            }
        }
        return map;
    }

    public String suggestWeakTopic(String username, String currentTopic) throws SQLException {
        String u = trim(username);
        if (u.isEmpty()) return currentTopic.isEmpty() ? "Revise your basics" : currentTopic;
        String sql = "SELECT topic, AVG(score) AS avg_score FROM results WHERE username=? "
                   + "GROUP BY topic ORDER BY avg_score ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String w = rs.getString("topic");
                    if (w != null && !w.isBlank()) return w;
                }
            }
        }
        return currentTopic.isEmpty() ? "Revise your basics" : currentTopic;
    }

    private static String trim(String v) { return v == null ? "" : v.trim(); }
}
