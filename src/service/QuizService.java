package service;

import database.DBConnection;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {
    public List<Question> fetchQuestionsByTopic(String topic) throws SQLException, ServiceException {
        String t = topic == null ? "" : topic.trim();
        if (t.isEmpty()) {
            throw new ValidationException("Topic is required.");
        }

        String sql = "SELECT id, topic, question, option1, option2, option3, option4, correct_answer " +
                "FROM questions WHERE topic = ? ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t);

            List<Question> questions = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(new Question(
                            rs.getInt("id"),
                            rs.getString("topic"),
                            rs.getString("question"),
                            rs.getString("option1"),
                            rs.getString("option2"),
                            rs.getString("option3"),
                            rs.getString("option4"),
                            rs.getString("correct_answer")
                    ));
                }
            }
            return questions;
        }
    }
}

