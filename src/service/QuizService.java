package service;

import database.DBConnection;
import model.Question;

import java.sql.*;
import java.util.*;

public class QuizService {

    // All 30 domain names
    public static final String[] ALL_DOMAINS = {
        "Data Structures and Algorithms", "Operating Systems",
        "Database Management Systems", "Computer Networks",
        "Object Oriented Programming", "Java", "Python",
        "C Programming", "C++", "Web Development",
        "HTML", "CSS", "JavaScript", "React", "SQL",
        "Software Engineering", "Cloud Computing", "Cyber Security",
        "Artificial Intelligence", "Machine Learning", "Data Science",
        "Aptitude", "Logical Reasoning", "Quantitative Aptitude",
        "Verbal Ability", "HR Interview", "Behavioral Interview",
        "Group Discussion Preparation", "System Design", "DevOps"
    };

    // Domain icons mapped by name
    public static String iconFor(String domain) {
        return switch (domain) {
            case "Data Structures and Algorithms" -> "📊";
            case "Operating Systems" -> "🖥️";
            case "Database Management Systems" -> "🗄️";
            case "Computer Networks" -> "🌐";
            case "Object Oriented Programming" -> "🧩";
            case "Java" -> "☕";
            case "Python" -> "🐍";
            case "C Programming" -> "⚙️";
            case "C++" -> "🔧";
            case "Web Development" -> "🕸️";
            case "HTML" -> "🏷️";
            case "CSS" -> "🎨";
            case "JavaScript" -> "⚡";
            case "React" -> "⚛️";
            case "SQL" -> "📋";
            case "Software Engineering" -> "🏗️";
            case "Cloud Computing" -> "☁️";
            case "Cyber Security" -> "🔒";
            case "Artificial Intelligence" -> "🤖";
            case "Machine Learning" -> "🧠";
            case "Data Science" -> "📈";
            case "Aptitude" -> "🧮";
            case "Logical Reasoning" -> "🔍";
            case "Quantitative Aptitude" -> "📐";
            case "Verbal Ability" -> "📝";
            case "HR Interview" -> "👔";
            case "Behavioral Interview" -> "💬";
            case "Group Discussion Preparation" -> "🎤";
            case "System Design" -> "🏛️";
            case "DevOps" -> "🚀";
            default -> "📚";
        };
    }

    // ─── Practice / Timed / Mock ─────────────────────────────────────────────

    public List<Question> fetchQuestionsByTopic(String topic)
            throws SQLException, ServiceException {
        return fetchQuestions(topic, null, 0);
    }

    public List<Question> fetchQuestionsByTopicAndDifficulty(String topic, String difficulty)
            throws SQLException, ServiceException {
        return fetchQuestions(topic, difficulty, 0);
    }

    /** Returns up to `limit` random questions. If limit <= 0, returns all. */
    public List<Question> fetchRandomQuestions(String topic, int limit)
            throws SQLException, ServiceException {
        return fetchQuestions(topic, null, limit);
    }

    private List<Question> fetchQuestions(String topic, String difficulty, int limit)
            throws SQLException, ServiceException {
        String t = topic == null ? "" : topic.trim();
        if (t.isEmpty()) throw new ValidationException("Topic is required.");

        StringBuilder sql = new StringBuilder(
            "SELECT id, topic, question, option1, option2, option3, option4, correct_answer, "
          + "COALESCE(difficulty,'Beginner') AS difficulty, "
          + "COALESCE(explanation,'') AS explanation, "
          + "COALESCE(topic_name, topic) AS topic_name "
          + "FROM questions WHERE topic = ?");
        if (difficulty != null && !difficulty.isBlank()) sql.append(" AND difficulty = ?");
        sql.append(" ORDER BY random()");
        if (limit > 0) sql.append(" LIMIT ").append(limit);

        List<Question> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, t);
            if (difficulty != null && !difficulty.isBlank()) ps.setString(2, difficulty);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    // ─── Mock Interview (10 random questions across difficulty levels) ────────

    public List<Question> fetchMockInterviewQuestions(String topic)
            throws SQLException, ServiceException {
        // 3 beginner, 4 intermediate, 3 advanced = 10 questions
        List<Question> all = new ArrayList<>();
        all.addAll(fetchQuestions(topic, "Beginner", 3));
        all.addAll(fetchQuestions(topic, "Intermediate", 4));
        all.addAll(fetchQuestions(topic, "Advanced", 3));
        Collections.shuffle(all);
        return all;
    }

    // ─── Company-Specific Patterns ───────────────────────────────────────────

    public List<Question> fetchCompanyQuestions(String company)
            throws SQLException, ServiceException {
        List<Question> out = new ArrayList<>();
        String[][] plan = companyPlan(company);
        for (String[] entry : plan) {
            String domain = entry[0];
            int count = Integer.parseInt(entry[1]);
            out.addAll(fetchRandomQuestions(domain, count));
        }
        Collections.shuffle(out);
        return out;
    }

    private String[][] companyPlan(String company) {
        return switch (company) {
            case "TCS" -> new String[][]{
                {"Aptitude", "5"}, {"C Programming", "3"}, {"Java", "3"},
                {"Data Structures and Algorithms", "4"}};
            case "Infosys" -> new String[][]{
                {"Aptitude", "4"}, {"Object Oriented Programming", "4"},
                {"Database Management Systems", "4"}, {"SQL", "3"}};
            case "Wipro" -> new String[][]{
                {"Verbal Ability", "3"}, {"Aptitude", "4"},
                {"C Programming", "4"}, {"Operating Systems", "4"}};
            case "Accenture" -> new String[][]{
                {"Logical Reasoning", "4"}, {"Web Development", "4"},
                {"Cloud Computing", "4"}, {"Cyber Security", "3"}};
            case "Cognizant" -> new String[][]{
                {"Aptitude", "4"}, {"Java", "4"},
                {"Database Management Systems", "4"}, {"Software Engineering", "3"}};
            case "Deloitte" -> new String[][]{
                {"Quantitative Aptitude", "4"}, {"SQL", "4"},
                {"Database Management Systems", "3"}, {"HR Interview", "4"}};
            case "UBS" -> new String[][]{
                {"Data Structures and Algorithms", "5"}, {"SQL", "5"},
                {"System Design", "5"}};
            case "AutoRABIT" -> new String[][]{
                {"DevOps", "5"}, {"Java", "5"},
                {"Software Engineering", "5"}};
            default -> new String[][]{
                {"Aptitude", "5"}, {"Data Structures and Algorithms", "5"},
                {"Java", "5"}};
        };
    }

    // ─── Daily Challenge ─────────────────────────────────────────────────────

    public List<Question> fetchDailyChallenge() throws SQLException, ServiceException {
        // Get all question IDs first
        List<Integer> ids = new ArrayList<>();
        String sqlIds = "SELECT id FROM questions";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlIds)) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        // Compute a day-based seed (year * 365 + day_of_year)
        Calendar cal = Calendar.getInstance();
        long seed = cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR) * 365L;
        Random rand = new Random(seed);

        // Shuffle IDs using the seeded Random and take up to 5
        List<Integer> shuffled = new ArrayList<>(ids);
        Collections.shuffle(shuffled, rand);
        List<Integer> selectedIds = shuffled.subList(0, Math.min(5, shuffled.size()));

        // Fetch the selected questions
        List<Question> out = new ArrayList<>();
        if (selectedIds.isEmpty()) {
            return out;
        }

        StringBuilder query = new StringBuilder(
            "SELECT id, topic, question, option1, option2, option3, option4, correct_answer, "
          + "COALESCE(difficulty,'Beginner') AS difficulty, "
          + "COALESCE(explanation,'') AS explanation, "
          + "COALESCE(topic_name, topic) AS topic_name "
          + "FROM questions WHERE id IN (");
        for (int i = 0; i < selectedIds.size(); i++) {
            query.append(selectedIds.get(i));
            if (i < selectedIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(")");

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query.toString())) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        }
        // Shuffle again to make sure order is not just by ID
        Collections.shuffle(out, rand);
        return out;
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    private Question mapRow(ResultSet rs) throws SQLException {
        return new Question(
            rs.getInt("id"), rs.getString("topic"),
            rs.getString("question"), rs.getString("option1"),
            rs.getString("option2"), rs.getString("option3"),
            rs.getString("option4"), rs.getString("correct_answer"),
            rs.getString("difficulty"), rs.getString("explanation"),
            rs.getString("topic_name"));
    }
}
