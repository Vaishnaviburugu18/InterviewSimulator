package model;

import java.sql.Timestamp;

public class ResultRecord {
    private final int id;
    private final String username;
    private final String topic;
    private final int scorePercent;
    private final Timestamp testDate;

    public ResultRecord(int id, String username, String topic, int scorePercent, Timestamp testDate) {
        this.id = id;
        this.username = username;
        this.topic = topic;
        this.scorePercent = scorePercent;
        this.testDate = testDate;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTopic() {
        return topic;
    }

    public int getScorePercent() {
        return scorePercent;
    }

    public Timestamp getTestDate() {
        return testDate;
    }
}

