package ui;

import model.Question;
import model.User;
import service.QuizService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.event.ActionListener;

public class QuizPage extends JFrame {
    private final String username;
    private final String topic;

    private final QuizService quizService = new QuizService();
    private List<Question> questions = Collections.emptyList();

    private int currentIndex = 0;
    private final List<String> selectedAnswers;

    private Timer questionTimer;
    private int timeRemainingSeconds = 30;

    private final JLabel questionCounterLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JTextArea questionTextArea = new JTextArea();

    private final JRadioButton option1Radio = new JRadioButton();
    private final JRadioButton option2Radio = new JRadioButton();
    private final JRadioButton option3Radio = new JRadioButton();
    private final JRadioButton option4Radio = new JRadioButton();
    private final ButtonGroup optionGroup = new ButtonGroup();

    private final JButton nextBtn = new JButton("Next");

    public QuizPage(String username, String topic) {
        this.username = username;
        this.topic = topic;

        this.selectedAnswers = new ArrayList<>();

        setTitle(topic + " - Mock Interview");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        buildUI();
        loadQuestionsAndStart();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel(topic + " Mock Interview");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        meta.setOpaque(false);
        questionCounterLabel.setForeground(Color.WHITE);
        timerLabel.setForeground(Color.WHITE);

        meta.add(questionCounterLabel);
        meta.add(timerLabel);

        top.add(heading, BorderLayout.WEST);
        top.add(meta, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        questionTextArea.setEditable(false);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        questionTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane questionScroll = new JScrollPane(questionTextArea);
        questionScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 120)));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(questionScroll, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 12, 12));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        optionsPanel.setOpaque(false);

        ActionListener enableNextListener = e -> nextBtn.setEnabled(getSelectedOptionText() != null);
        for (JRadioButton b : new JRadioButton[] {option1Radio, option2Radio, option3Radio, option4Radio}) {
            b.setFont(new Font("Arial", Font.PLAIN, 16));
            b.setOpaque(false);
            optionGroup.add(b);
            b.addActionListener(enableNextListener);
            optionsPanel.add(b);
        }

        center.add(optionsPanel, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        bottom.setOpaque(false);
        nextBtn.setFont(new Font("Arial", Font.BOLD, 14));
        nextBtn.setEnabled(false);
        bottom.add(nextBtn);
        root.add(bottom, BorderLayout.SOUTH);

        nextBtn.addActionListener(e -> {
            String selected = getSelectedOptionText();
            storeAnswerForCurrentIndex(selected);
            goToNext();
        });

        setContentPane(root);
    }

    private void loadQuestionsAndStart() {
        SwingUtilities.invokeLater(() -> {
            try {
                questions = quizService.fetchQuestionsByTopic(topic);
                selectedAnswers.clear();
                for (int i = 0; i < questions.size(); i++) selectedAnswers.add(null);

                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No questions found for topic: " + topic,
                            "No Questions", JOptionPane.WARNING_MESSAGE);
                    new Dashboard(new User(-1, username, "")).setVisible(true);
                    dispose();
                    return;
                }

                currentIndex = 0;
                showQuestion(currentIndex);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                new Dashboard(new User(-1, username, "")).setVisible(true);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                new Dashboard(new User(-1, username, "")).setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                new Dashboard(new User(-1, username, "")).setVisible(true);
                dispose();
            }
        });
    }

    private void showQuestion(int index) {
        stopTimerIfRunning();
        optionGroup.clearSelection();
        enableOptions(true);
        nextBtn.setEnabled(false);

        Question q = questions.get(index);
        questionCounterLabel.setText("Q " + (index + 1) + " / " + questions.size());

        option1Radio.setText(q.getOption1());
        option2Radio.setText(q.getOption2());
        option3Radio.setText(q.getOption3());
        option4Radio.setText(q.getOption4());

        questionTextArea.setText(q.getQuestion());

        updateTimerLabel(30);
        timeRemainingSeconds = 30;

        // Start timer.
        questionTimer = new Timer(1000, e -> {
            timeRemainingSeconds--;
            updateTimerLabel(timeRemainingSeconds);
            if (timeRemainingSeconds <= 0) {
                storeAnswerForCurrentIndex(null);
                stopTimerIfRunning();
                disableOptionsWhileMoving();
                goToNextAfterTimeout();
            }
        });
        questionTimer.setInitialDelay(0);
        questionTimer.start();
    }

    private void updateTimerLabel(int seconds) {
        timerLabel.setText("Time: " + seconds + "s");
    }

    private String getSelectedOptionText() {
        if (option1Radio.isSelected()) return option1Radio.getText();
        if (option2Radio.isSelected()) return option2Radio.getText();
        if (option3Radio.isSelected()) return option3Radio.getText();
        if (option4Radio.isSelected()) return option4Radio.getText();
        return null;
    }

    private void storeAnswerForCurrentIndex(String answer) {
        if (currentIndex < 0 || currentIndex >= selectedAnswers.size()) return;
        selectedAnswers.set(currentIndex, answer);
    }

    private void enableOptions(boolean enable) {
        option1Radio.setEnabled(enable);
        option2Radio.setEnabled(enable);
        option3Radio.setEnabled(enable);
        option4Radio.setEnabled(enable);
    }

    private void disableOptionsWhileMoving() {
        enableOptions(false);
        nextBtn.setEnabled(false);
    }

    private void stopTimerIfRunning() {
        if (questionTimer != null) {
            questionTimer.stop();
            questionTimer = null;
        }
    }

    private void goToNextAfterTimeout() {
        // Give a short delay so the user sees timer stop.
        new javax.swing.Timer(600, e -> goToNext()).start();
    }

    private void goToNext() {
        stopTimerIfRunning();

        if (currentIndex >= selectedAnswers.size() - 1) {
            // Finish quiz.
            dispose();
            new ResultPage(username, topic, questions, selectedAnswers);
            return;
        }

        currentIndex++;
        showQuestion(currentIndex);
    }
}

