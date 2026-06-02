package ui;

import model.Question;
import model.User;
import service.ResultService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ResultPage extends JFrame {
    private final String username;
    private final String topic;
    private final List<Question> questions;
    private final List<String> selectedAnswers;

    private final ResultService resultService = new ResultService();

    private boolean resultSaved = false;

    private final JLabel scoreLabel = new JLabel();
    private final JLabel weakTopicLabel = new JLabel();

    public ResultPage(String username, String topic, List<Question> questions, List<String> selectedAnswers) {
        this.username = username;
        this.topic = topic;
        this.questions = questions;
        this.selectedAnswers = selectedAnswers;

        setTitle("Result - " + topic);
        setSize(980, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        buildUI();

        // Calculate in-memory immediately; saving to DB happens next.
        int total = questions.size();
        int correct = 0;
        for (int i = 0; i < total; i++) {
            String selected = selectedAnswers.size() > i ? selectedAnswers.get(i) : null;
            if (selected != null && selected.trim().equalsIgnoreCase(questions.get(i).getCorrectAnswer().trim())) {
                correct++;
            }
        }
        int percent = total == 0 ? 0 : (correct * 100) / total;

        scoreLabel.setText("Your Score: " + percent + "% (" + correct + " / " + total + " correct)");

        // Render answer analysis.
        JPanel analysis = new JPanel();
        analysis.setLayout(new BoxLayout(analysis, BoxLayout.Y_AXIS));
        analysis.setBackground(new Color(15, 23, 42));

        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            String selected = selectedAnswers.size() > i ? selectedAnswers.get(i) : null;
            String correctAns = q.getCorrectAnswer();

            boolean isCorrect = selected != null && selected.trim().equalsIgnoreCase(correctAns.trim());

            JPanel item = new JPanel(new GridLayout(4, 1, 4, 4));
            item.setBackground(new Color(21, 32, 54));
            item.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 120)));

            JLabel qLabel = new JLabel((i + 1) + ". " + q.getQuestion());
            qLabel.setForeground(Color.WHITE);

            JLabel yourLabel = new JLabel("Your answer: " + (selected == null ? "Not answered" : selected));
            yourLabel.setForeground(Color.WHITE);

            JLabel correctLabel = new JLabel("Correct answer: " + correctAns);
            correctLabel.setForeground(Color.WHITE);

            JLabel resLabel = new JLabel(isCorrect ? "Result: Correct" : "Result: Wrong");
            resLabel.setForeground(isCorrect ? new Color(46, 204, 113) : new Color(231, 76, 60));
            resLabel.setFont(resLabel.getFont().deriveFont(Font.BOLD));

            item.add(qLabel);
            item.add(yourLabel);
            item.add(correctLabel);
            item.add(resLabel);
            analysis.add(item);
            analysis.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(analysis);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(15, 23, 42));

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            new Dashboard(new User(-1, username, "")).setVisible(true);
            dispose();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(15, 23, 42));

        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));

        weakTopicLabel.setForeground(new Color(160, 190, 255));
        weakTopicLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        weakTopicLabel.setText("Suggested weak topic: Loading...");

        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(scoreLabel);
        labels.add(weakTopicLabel);
        top.add(labels, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        bottom.setBackground(new Color(15, 23, 42));
        bottom.add(backBtn);

        setLayout(new BorderLayout(10, 10));
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        saveResultOnce(percent);
    }

    private void buildUI() {
        // Intentionally minimal; main UI is built after score calculation.
    }

    private void saveResultOnce(int scorePercent) {
        if (resultSaved) return;
        resultSaved = true;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    resultService.saveResult(username, topic, scorePercent);
                } catch (ServiceException ex) {
                    // Ignore save errors for rendering; show message on EDT after.
                    throw new RuntimeException(ex);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    String weak = resultService.suggestWeakTopic(username, topic);
                    weakTopicLabel.setText("Suggested weak topic: " + weak);
                } catch (Exception ex) {
                    weakTopicLabel.setText("Suggested weak topic: (could not load)");
                    JOptionPane.showMessageDialog(ResultPage.this,
                            "Could not save result: " + (ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}

