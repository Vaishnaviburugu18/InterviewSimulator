package service;

import database.DBConnection;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminService {
    public void addQuestion(Question q) throws ServiceException, SQLException {
        validateQuestion(q);

        String sql = "INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q.getTopic().trim());
            stmt.setString(2, q.getQuestion().trim());
            stmt.setString(3, q.getOption1().trim());
            stmt.setString(4, q.getOption2().trim());
            stmt.setString(5, q.getOption3().trim());
            stmt.setString(6, q.getOption4().trim());
            stmt.setString(7, q.getCorrectAnswer().trim());
            stmt.executeUpdate();
        }
    }

    public void updateQuestion(Question q) throws ServiceException, SQLException {
        if (q.getId() <= 0) {
            throw new ValidationException("Question ID must be provided for update.");
        }
        validateQuestion(q);

        String sql = "UPDATE questions SET topic=?, question=?, option1=?, option2=?, option3=?, option4=?, correct_answer=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q.getTopic().trim());
            stmt.setString(2, q.getQuestion().trim());
            stmt.setString(3, q.getOption1().trim());
            stmt.setString(4, q.getOption2().trim());
            stmt.setString(5, q.getOption3().trim());
            stmt.setString(6, q.getOption4().trim());
            stmt.setString(7, q.getCorrectAnswer().trim());
            stmt.setInt(8, q.getId());
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

