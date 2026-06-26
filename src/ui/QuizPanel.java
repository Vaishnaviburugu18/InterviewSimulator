package ui;

import model.Question;
import model.User;
import service.QuizService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redesigned, production-quality Quiz Panel.
 * Implements a modern dark theme, auto-saves answer choices, manages a stable timer,
 * and features fully custom styled options (cards) with hover/selected effects.
 */
public class QuizPanel extends JPanel {

    private final MainFrame frame;
    private final User user;
    private final String domain;
    private final String mode;
    private final String company;
    private final QuizService quizService = new QuizService();

    private List<Question> questions = Collections.emptyList();
    private List<String> answers;
    private int idx = 0;
    private Timer countdown;
    private int timeLeft = 30;
    private boolean isSubmitted = false;

    // UI Components
    private final JLabel topicLabel = new JLabel();
    private final JLabel qNumLabel = new JLabel();
    private final JLabel timerLabel = new JLabel("30s");
    private final JLabel modeLabel = new JLabel();
    private final JLabel difficultyBadge = new JLabel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    };
    private final JProgressBar progBar = new JProgressBar();

    private final JTextArea questionText = new JTextArea();
    private final ButtonGroup optGroup = new ButtonGroup();
    private final JRadioButton[] opts = new JRadioButton[4];
    private final JPanel[] optPanels = new JPanel[4];

    private final ModernButton prevBtn = new ModernButton("Previous");
    private final ModernButton nextBtn = new ModernButton("Next");
    private final ModernButton clearBtn = new ModernButton("Clear Choice");
    private final ModernButton submitBtn = new ModernButton("Finish Quiz");

    private final JPanel navGrid = new JPanel(new GridLayout(0, 5, 6, 6));

    public QuizPanel(MainFrame frame, User user, String domain, String mode, String company) {
        this.frame = frame;
        this.user = user;
        this.domain = domain;
        this.mode = mode;
        this.company = company;

        setBackground(Theme.DARK_BG);
        setLayout(new BorderLayout());
        buildUI();
        loadQuestions();
    }

    private void buildUI() {
        // ─── TOP BAR (Header Info) ──────────────────────────────────────────
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setBackground(Theme.SIDEBAR_BG);
        topBar.setBorder(new EmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);

        topicLabel.setFont(Theme.TITLE_FONT);
        topicLabel.setForeground(Color.WHITE);
        row1.add(topicLabel, BorderLayout.WEST);

        JPanel rightRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightRow1.setOpaque(false);

        modeLabel.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        modeLabel.setForeground(Theme.ACCENT);
        rightRow1.add(modeLabel);

        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(Theme.SUCCESS);
        timerLabel.setIconTextGap(8);
        rightRow1.add(timerLabel);

        row1.add(rightRow1, BorderLayout.EAST);
        topBar.add(row1, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 0, 0);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row2.setOpaque(false);

        difficultyBadge.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        difficultyBadge.setForeground(Color.WHITE);
        difficultyBadge.setOpaque(false);
        difficultyBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        row2.add(difficultyBadge);

        row2.add(Box.createHorizontalStrut(16));

        qNumLabel.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        qNumLabel.setForeground(Theme.DARK_TEXT_MAIN);
        row2.add(qNumLabel);

        topBar.add(row2, gbc);
        add(topBar, BorderLayout.NORTH);

        // ─── CENTER CONTAINER (Left Card + Right Navigation) ────────────────
        JPanel mainContainer = new JPanel(new BorderLayout(24, 0));
        mainContainer.setBackground(Theme.DARK_BG);
        mainContainer.setBorder(new EmptyBorder(24, 24, 24, 24));

        // 1. Question Card (Left)
        RoundedPanel qCard = new RoundedPanel(16, Theme.DARK_CARD);
        qCard.setLayout(new BorderLayout(0, 16));
        qCard.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Progress bar inside the card
        JPanel progPanel = new JPanel(new BorderLayout());
        progPanel.setOpaque(false);
        progBar.setForeground(Theme.ACCENT);
        progBar.setBackground(new Color(15, 23, 42));
        progBar.setStringPainted(false);
        progBar.setPreferredSize(new Dimension(0, 6));
        progPanel.add(progBar, BorderLayout.CENTER);
        qCard.add(progPanel, BorderLayout.NORTH);

        // Question text
        questionText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        questionText.setBackground(Theme.DARK_CARD);
        questionText.setForeground(Theme.DARK_TEXT_MAIN);
        questionText.setEditable(false);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        questionText.setOpaque(false);
        questionText.setBorder(new EmptyBorder(8, 0, 12, 0));
        qCard.add(questionText, BorderLayout.CENTER);

        // Options List (Custom Styled Cards)
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(4, 1, 0, 12));
        optionsPanel.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            opts[i] = new JRadioButton();
            optGroup.add(opts[i]);
            
            // Build modern Option Container
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Theme.DARK_CARD);
            card.setBorder(BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            opts[i].setFont(Theme.BODY_FONT);
            opts[i].setForeground(Theme.DARK_TEXT_MAIN);
            opts[i].setFocusPainted(false);
            opts[i].setOpaque(false);
            opts[i].setBorder(new EmptyBorder(14, 16, 14, 16));

            card.add(opts[i], BorderLayout.CENTER);
            optPanels[i] = card;
            optionsPanel.add(card);

            final int optIdx = i;
            // Hover and Click Action Bindings
            opts[i].addActionListener(e -> {
                saveCurrentAnswer();
                updateOptionStyles();
                updateNavGrid();
            });
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!opts[optIdx].isSelected()) {
                        card.setBackground(new Color(51, 65, 85));
                        card.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1, true));
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!opts[optIdx].isSelected()) {
                        card.setBackground(Theme.DARK_CARD);
                        card.setBorder(BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true));
                    }
                }
                public void mouseClicked(MouseEvent e) {
                    opts[optIdx].setSelected(true);
                    saveCurrentAnswer();
                    updateOptionStyles();
                    updateNavGrid();
                }
            });
        }
        qCard.add(optionsPanel, BorderLayout.SOUTH);
        mainContainer.add(qCard, BorderLayout.CENTER);

        // 2. Navigation Sidebar (Right)
        JPanel rightSidebar = new JPanel(new BorderLayout(0, 16));
        rightSidebar.setOpaque(false);
        rightSidebar.setPreferredSize(new Dimension(200, 0));

        RoundedPanel navCard = new RoundedPanel(16, Theme.DARK_CARD);
        navCard.setLayout(new BorderLayout(0, 16));
        navCard.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel navTitle = new JLabel("Question Navigation");
        navTitle.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        navTitle.setForeground(Theme.DARK_TEXT_SUB);
        navCard.add(navTitle, BorderLayout.NORTH);

        navGrid.setOpaque(false);
        navCard.add(navGrid, BorderLayout.CENTER);

        // Quick Legend
        JPanel legend = new JPanel(new GridLayout(3, 1, 0, 6));
        legend.setOpaque(false);
        legend.add(createLegendItem("● Current", Theme.ACCENT));
        legend.add(createLegendItem("● Answered", Theme.SUCCESS));
        legend.add(createLegendItem("● Unanswered", Theme.DARK_BORDER));
        navCard.add(legend, BorderLayout.SOUTH);

        rightSidebar.add(navCard, BorderLayout.NORTH);
        mainContainer.add(rightSidebar, BorderLayout.EAST);

        add(mainContainer, BorderLayout.CENTER);

        // ─── BOTTOM NAVIGATION BAR ──────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout(16, 0));
        bottomBar.setBackground(Theme.SIDEBAR_BG);
        bottomBar.setBorder(new EmptyBorder(16, 24, 16, 24));

        prevBtn.setColors(new Color(51, 65, 85), new Color(71, 85, 105));
        prevBtn.setIcon(IconFactory.getIcon("prev", 14, Color.WHITE));
        prevBtn.setIconTextGap(8);
        prevBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        nextBtn.setColors(Theme.ACCENT, Theme.ACCENT_HOVER);
        nextBtn.setIcon(IconFactory.getIcon("next", 14, Color.WHITE));
        nextBtn.setHorizontalTextPosition(SwingConstants.LEFT);
        nextBtn.setIconTextGap(8);
        nextBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        clearBtn.setColors(new Color(30, 41, 59), new Color(51, 65, 85));
        clearBtn.setIcon(IconFactory.getIcon("cross", 12, Color.WHITE));
        clearBtn.setIconTextGap(8);
        clearBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        submitBtn.setColors(Theme.SUCCESS, Theme.SUCCESS.darker());
        submitBtn.setIcon(IconFactory.getIcon("check", 14, Color.WHITE));
        submitBtn.setIconTextGap(8);
        submitBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBottom.setOpaque(false);
        leftBottom.add(prevBtn);
        leftBottom.add(clearBtn);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBottom.setOpaque(false);
        rightBottom.add(nextBtn);
        rightBottom.add(submitBtn);

        bottomBar.add(leftBottom, BorderLayout.WEST);
        bottomBar.add(rightBottom, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        // Button Actions
        prevBtn.addActionListener(e -> navigate(idx - 1));
        nextBtn.addActionListener(e -> navigate(idx + 1));
        clearBtn.addActionListener(e -> clearCurrentSelection());
        submitBtn.addActionListener(e -> confirmSubmit());
    }

    private void loadQuestions() {
        String title = company != null ? company + " Assessment" : (domain != null ? domain : "General Exam");
        topicLabel.setText(title);
        topicLabel.setIcon(IconFactory.getIcon("book", 20, Theme.ACCENT));
        topicLabel.setIconTextGap(10);
        modeLabel.setText(mode + " Mode");

        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws Exception {
                if ("Company".equalsIgnoreCase(mode)) {
                    return quizService.fetchCompanyQuestions(company);
                } else if ("Mock".equalsIgnoreCase(mode)) {
                    return quizService.fetchMockInterviewQuestions(domain);
                } else if ("Daily Challenge".equalsIgnoreCase(mode)) {
                    return quizService.fetchDailyChallenge();
                } else {
                    return quizService.fetchRandomQuestions(domain, 10);
                }
            }

            @Override
            protected void done() {
                try {
                    questions = get();
                    if (questions.isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                                "No questions found for this topic/mode.\nPlease verify that your SQLite database contains question records.",
                                "Empty Topic", JOptionPane.WARNING_MESSAGE);
                        frame.goToDashboard(user);
                        return;
                    }
                    answers = new ArrayList<>(Collections.nCopies(questions.size(), null));
                    buildNavGrid();
                    showQuestion(0);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading questions: " + ex.getMessage());
                    frame.goToDashboard(user);
                }
            }
        };
        worker.execute();
    }

    private void buildNavGrid() {
        navGrid.removeAll();
        for (int i = 0; i < questions.size(); i++) {
            final int qi = i;
            NavButton b = new NavButton(String.valueOf(i + 1));
            b.addActionListener(e -> {
                saveCurrentAnswer();
                navigate(qi);
            });
            navGrid.add(b);
        }
        navGrid.revalidate();
        navGrid.repaint();
    }

    private void navigate(int newIdx) {
        if (newIdx < 0 || newIdx >= questions.size()) return;
        saveCurrentAnswer();
        showQuestion(newIdx);
    }

    private void showQuestion(int i) {
        stopTimer();
        this.idx = i;
        Question q = questions.get(i);

        // Update indicators
        qNumLabel.setText("Question " + (i + 1) + " of " + questions.size());
        progBar.setMaximum(questions.size());
        progBar.setValue(i + 1);
        questionText.setText(q.getQuestion());

        // Update difficulty badge
        String diff = q.getDifficulty();
        Color bgBadge = "Advanced".equalsIgnoreCase(diff) ? Theme.DANGER :
                        "Intermediate".equalsIgnoreCase(diff) ? Theme.WARNING : Theme.SUCCESS;
        difficultyBadge.setBackground(bgBadge);
        difficultyBadge.setText(diff.toUpperCase());

        // Fill options
        optGroup.clearSelection();
        for (int j = 0; j < 4; j++) {
            opts[j].setText(q.getOption(j));
            opts[j].setSelected(false);
        }

        // Restore answered choice
        String prevAnswer = answers.get(i);
        if (prevAnswer != null) {
            for (JRadioButton opt : opts) {
                if (opt.getText().trim().equalsIgnoreCase(prevAnswer.trim())) {
                    opt.setSelected(true);
                    break;
                }
            }
        }

        updateOptionStyles();

        // Control buttons states
        prevBtn.setEnabled(i > 0);
        nextBtn.setVisible(i < questions.size() - 1);
        
        // Highlight submit button on the last question
        if (i == questions.size() - 1) {
            submitBtn.setText("Finish Quiz");
            submitBtn.setColors(Theme.SUCCESS, Theme.SUCCESS.darker());
        } else {
            submitBtn.setText("Early Submit");
            submitBtn.setColors(new Color(51, 65, 85), new Color(71, 85, 105));
        }

        // Handle Mode timer
        if ("Timed".equalsIgnoreCase(mode) || "Mock".equalsIgnoreCase(mode)) {
            startTimer();
        } else {
            timerLabel.setText("Unlimited Time");
            timerLabel.setIcon(IconFactory.getIcon("timer", 18, Theme.DARK_TEXT_SUB));
            timerLabel.setForeground(Theme.DARK_TEXT_SUB);
        }

        updateNavGrid();
    }

    private void updateOptionStyles() {
        for (int i = 0; i < 4; i++) {
            boolean sel = opts[i].isSelected();
            optPanels[i].setBackground(sel ? new Color(99, 102, 241, 45) : Theme.DARK_CARD);
            optPanels[i].setBorder(BorderFactory.createLineBorder(sel ? Theme.ACCENT : Theme.DARK_BORDER, 1, true));
        }
    }

    private void updateNavGrid() {
        Component[] btns = navGrid.getComponents();
        for (int i = 0; i < btns.length && i < questions.size(); i++) {
            NavButton b = (NavButton) btns[i];
            b.setStates(i == idx, answers.get(i) != null);
        }
    }

    private void saveCurrentAnswer() {
        if (answers == null || idx >= answers.size()) return;
        String selection = null;
        for (JRadioButton opt : opts) {
            if (opt.isSelected()) {
                selection = opt.getText();
                break;
            }
        }
        answers.set(idx, selection);
    }

    private void clearCurrentSelection() {
        optGroup.clearSelection();
        if (answers != null && idx < answers.size()) {
            answers.set(idx, null);
        }
        updateOptionStyles();
        updateNavGrid();
    }

    // ─── Timer Handling ──────────────────────────────────────────────────────
    private void startTimer() {
        stopTimer();
        timeLeft = 30;
        timerLabel.setForeground(Theme.SUCCESS);
        timerLabel.setIcon(IconFactory.getIcon("timer", 18, Theme.SUCCESS));
        timerLabel.setText(timeLeft + "s");

        countdown = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText(timeLeft + "s");
            
            if (timeLeft <= 5) {
                timerLabel.setForeground(Theme.DANGER);
                timerLabel.setIcon(IconFactory.getIcon("timer", 18, Theme.DANGER));
            } else if (timeLeft <= 12) {
                timerLabel.setForeground(Theme.WARNING);
                timerLabel.setIcon(IconFactory.getIcon("timer", 18, Theme.WARNING));
            }

            if (timeLeft <= 0) {
                stopTimer();
                saveCurrentAnswer();
                if (idx < questions.size() - 1) {
                    navigate(idx + 1);
                } else {
                    submitQuiz();
                }
            }
        });
        countdown.start();
    }

    private void stopTimer() {
        if (countdown != null && countdown.isRunning()) {
            countdown.stop();
        }
    }

    // ─── Quiz Submission ────────────────────────────────────────────────────
    private void confirmSubmit() {
        stopTimer();
        saveCurrentAnswer();
        long answeredCount = answers.stream().filter(a -> a != null).count();
        int unanswered = questions.size() - (int) answeredCount;

        String msg = unanswered > 0
                ? "You have " + unanswered + " unanswered question(s). Are you sure you want to finish the quiz?"
                : "All questions answered. Submit and view results?";

        int choice = JOptionPane.showConfirmDialog(frame, msg, "Finish Quiz",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            submitQuiz();
        } else {
            if ("Timed".equalsIgnoreCase(mode) || "Mock".equalsIgnoreCase(mode)) {
                startTimer();
            }
        }
    }

    private void submitQuiz() {
        if (isSubmitted) return; // Prevent double submission
        isSubmitted = true;
        stopTimer();
        saveCurrentAnswer();

        // Navigate to Result screen
        String finalDomain = domain != null ? domain : (company != null ? company + " Exam" : "Practice Quiz");
        
        // Find most frequent difficulty from questions to save it
        String finalDiff = "Mixed";
        if (questions != null && !questions.isEmpty()) {
            finalDiff = questions.get(0).getDifficulty();
        }

        frame.showResult(user, finalDomain, mode, questions, answers);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private JLabel createLegendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL_FONT);
        l.setForeground(color);
        l.setBorder(new EmptyBorder(2, 4, 2, 4));
        return l;
    }

    private static class NavButton extends JButton {
        private boolean selected = false;
        private boolean answered = false;

        public NavButton(String text) {
            super(text);
            setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(Theme.DARK_TEXT_SUB);
            setPreferredSize(new Dimension(34, 34));
        }

        public void setStates(boolean selected, boolean answered) {
            this.selected = selected;
            this.answered = answered;
            if (selected) {
                setForeground(Color.WHITE);
            } else if (answered) {
                setForeground(Color.WHITE);
            } else {
                setForeground(Theme.DARK_TEXT_SUB);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (answered) {
                g2.setColor(Theme.SUCCESS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else {
                g2.setColor(Theme.DARK_CARD.brighter());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Theme.DARK_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
