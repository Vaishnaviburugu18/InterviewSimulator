package service;

import database.DBConnection;
import model.ResultRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultService {
    public void saveResult(String username, String topic, int scorePercent) throws ServiceException, SQLException {
        String u = trimOrEmpty(username);
        String t = trimOrEmpty(topic);
        if (u.isEmpty() || t.isEmpty()) {
            throw new ValidationException("Username/topic are required to save results.");
        }
        int s = Math.max(0, Math.min(100, scorePercent));

        String sql = "INSERT INTO results (username, topic, score) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u);
            stmt.setString(2, t);
            stmt.setInt(3, s);
            stmt.executeUpdate();
        }
    }

    public List<ResultRecord> getPreviousResults(String username) throws ServiceException, SQLException {
        String u = trimOrEmpty(username);
        if (u.isEmpty()) {
            throw new ValidationException("Username is required.");
        }

        String sql = "SELECT id, username, topic, score, test_date FROM results WHERE username = ? " +
                "ORDER BY test_date DESC LIMIT 100";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u);

            List<ResultRecord> records = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String uname = rs.getString("username");
                    String topic = rs.getString("topic");
                    int score = rs.getInt("score");
                    Timestamp ts = rs.getTimestamp("test_date");
                    records.add(new ResultRecord(id, uname, topic, score, ts));
                }
            }
            return records;
        }
    }

    public String suggestWeakTopic(String username, String currentTopic) throws SQLException {
        // Basic logic: pick the topic with lowest average score for this user.
        // If no results exist yet, suggest revising the current topic.
        String u = trimOrEmpty(username);
        String current = trimOrEmpty(currentTopic);
        if (u.isEmpty()) return current.isEmpty() ? "Revise your basics" : current;

        String sql = "SELECT topic, AVG(score) AS avg_score FROM results WHERE username = ? " +
                "GROUP BY topic ORDER BY avg_score ASC LIMIT 1";

        Map<String, Integer> knownTopics = new HashMap<>();
        // knownTopics is not required but makes it easy to expand in the future.

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String weakTopic = rs.getString("topic");
                    if (weakTopic != null && !weakTopic.isBlank()) return weakTopic;
                }
            }
        }
        return current.isEmpty() ? "Revise your basics" : current;
    }

    private static String trimOrEmpty(String v) {
        return v == null ? "" : v.trim();
    }
}

