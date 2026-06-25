package service;

import database.DBConnection;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminService {

    public void addQuestion(Question q) throws ServiceException, SQLException {
        validateQuestion(q);

        String sql = "INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer, difficulty, explanation, topic_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q.getTopic().trim());
            stmt.setString(2, q.getQuestion().trim());
            stmt.setString(3, q.getOption1().trim());
            stmt.setString(4, q.getOption2().trim());
            stmt.setString(5, q.getOption3().trim());
            stmt.setString(6, q.getOption4().trim());
            stmt.setString(7, q.getCorrectAnswer().trim());
            stmt.setString(8, q.getDifficulty().trim());
            stmt.setString(9, q.getExplanation() == null ? "" : q.getExplanation().trim());
            stmt.setString(10, q.getTopicName() == null ? q.getTopic().trim() : q.getTopicName().trim());
            stmt.executeUpdate();
        }
    }

    public void updateQuestion(Question q) throws ServiceException, SQLException {
        if (q.getId() <= 0) {
            throw new ValidationException("Question ID must be provided for update.");
        }
        validateQuestion(q);

        String sql = "UPDATE questions SET topic=?, question=?, option1=?, option2=?, option3=?, option4=?, correct_answer=?, difficulty=?, explanation=?, topic_name=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q.getTopic().trim());
            stmt.setString(2, q.getQuestion().trim());
            stmt.setString(3, q.getOption1().trim());
            stmt.setString(4, q.getOption2().trim());
            stmt.setString(5, q.getOption3().trim());
            stmt.setString(6, q.getOption4().trim());
            stmt.setString(7, q.getCorrectAnswer().trim());
            stmt.setString(8, q.getDifficulty().trim());
            stmt.setString(9, q.getExplanation() == null ? "" : q.getExplanation().trim());
            stmt.setString(10, q.getTopicName() == null ? q.getTopic().trim() : q.getTopicName().trim());
            stmt.setInt(11, q.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteQuestion(int questionId) throws SQLException, ServiceException {
        if (questionId <= 0) {
            throw new ValidationException("Question ID must be a positive integer.");
        }
        String sql = "DELETE FROM questions WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            stmt.executeUpdate();
        }
    }

    public List<Question> getAllQuestions() throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT id, topic, question, option1, option2, option3, option4, correct_answer, "
                   + "COALESCE(difficulty,'Beginner') AS difficulty, "
                   + "COALESCE(explanation,'') AS explanation, "
                   + "COALESCE(topic_name, topic) AS topic_name "
                   + "FROM questions ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Question(
                    rs.getInt("id"), rs.getString("topic"),
                    rs.getString("question"), rs.getString("option1"),
                    rs.getString("option2"), rs.getString("option3"),
                    rs.getString("option4"), rs.getString("correct_answer"),
                    rs.getString("difficulty"), rs.getString("explanation"),
                    rs.getString("topic_name")
                ));
            }
        }
        return list;
    }

    public List<Question> searchQuestions(String query, String topic, String difficulty) throws SQLException {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, topic, question, option1, option2, option3, option4, correct_answer, "
          + "COALESCE(difficulty,'Beginner') AS difficulty, "
          + "COALESCE(explanation,'') AS explanation, "
          + "COALESCE(topic_name, topic) AS topic_name "
          + "FROM questions WHERE 1=1");
        
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append(" AND (question LIKE ? OR topic_name LIKE ?)");
            params.add("%" + query.trim() + "%");
            params.add("%" + query.trim() + "%");
        }
        if (topic != null && !topic.equals("All Domains") && !topic.isBlank()) {
            sql.append(" AND topic = ?");
            params.add(topic);
        }
        if (difficulty != null && !difficulty.equals("All Difficulties") && !difficulty.isBlank()) {
            sql.append(" AND difficulty = ?");
            params.add(difficulty);
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Question(
                        rs.getInt("id"), rs.getString("topic"),
                        rs.getString("question"), rs.getString("option1"),
                        rs.getString("option2"), rs.getString("option3"),
                        rs.getString("option4"), rs.getString("correct_answer"),
                        rs.getString("difficulty"), rs.getString("explanation"),
                        rs.getString("topic_name")
                    ));
                }
            }
        }
        return list;
    }

    public Map<String, Integer> getQuestionStats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT topic, COUNT(*) as cnt FROM questions GROUP BY topic ORDER BY cnt DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int total = 0;
            while (rs.next()) {
                String topic = rs.getString("topic");
                int cnt = rs.getInt("cnt");
                stats.put(topic, cnt);
                total += cnt;
            }
            stats.put("Total Questions", total);
        }
        return stats;
    }

    private void validateQuestion(Question q) throws ValidationException {
        if (q == null) throw new ValidationException("Question is required.");
        if (isBlank(q.getTopic()) || isBlank(q.getQuestion())) throw new ValidationException("Topic and question are required.");
        if (isBlank(q.getOption1()) || isBlank(q.getOption2()) || isBlank(q.getOption3()) || isBlank(q.getOption4())) {
            throw new ValidationException("All four options are required.");
        }
        if (isBlank(q.getCorrectAnswer())) throw new ValidationException("Correct answer is required.");

        String ca = q.getCorrectAnswer().trim();
        if (!equalsAny(ca, q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4())) {
            throw new ValidationException("Correct answer must match exactly one of the options.");
        }
    }

    private boolean equalsAny(String target, String... candidates) {
        for (String c : candidates) {
            if (c != null && c.trim().equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
