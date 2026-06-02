package model;

public class Question {
    private final int id;
    private final String topic;
    private final String question;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final String correctAnswer;

    public Question(
            int id,
            String topic,
            String question,
            String option1,
            String option2,
            String option3,
            String option4,
            String correctAnswer) {
        this.id = id;
        this.topic = topic;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
    }

    public int getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getOption(int index0To3) {
        return switch (index0To3) {
            case 0 -> option1;
            case 1 -> option2;
            case 2 -> option3;
            case 3 -> option4;
            default -> throw new IllegalArgumentException("Option index must be 0..3");
        };
    }
}

