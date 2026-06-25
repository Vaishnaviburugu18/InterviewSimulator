package model;

import java.sql.Timestamp;

public class ResultRecord {
    private final int id;
    private final String username;
    private final String topic;
    private final int scorePercent;
    private final Timestamp testDate;
    private final String mode;
    private final int correctCount;
    private final int totalCount;
    private final String difficulty;

    // Full constructor
    public ResultRecord(int id, String username, String topic, int scorePercent,
                        Timestamp testDate, String mode, int correctCount, int totalCount, String difficulty) {
        this.id = id;
        this.username = username;
        this.topic = topic;
        this.scorePercent = scorePercent;
        this.testDate = testDate;
        this.mode = mode != null ? mode : "Practice";
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.difficulty = difficulty != null ? difficulty : "Mixed";
    }

    // Legacy constructor (backward compatibility)
    public ResultRecord(int id, String username, String topic, int scorePercent, Timestamp testDate) {
        this(id, username, topic, scorePercent, testDate, "Practice", 0, 0, "Mixed");
    }

    // Constructor used by legacy code
    public ResultRecord(String username, String topic, int scorePercent, Timestamp testDate) {
        this(-1, username, topic, scorePercent, testDate, "Practice", 0, 0, "Mixed");
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getTopic() { return topic; }
    public int getScorePercent() { return scorePercent; }
    public Timestamp getTestDate() { return testDate; }
    public String getMode() { return mode; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalCount() { return totalCount; }
    public String getDifficulty() { return difficulty; }
}
