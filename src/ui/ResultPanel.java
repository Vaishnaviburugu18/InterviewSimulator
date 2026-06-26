package ui;

import model.Question;
import model.User;
import service.ProfileService;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/** Result screen with circular score, detailed review, strengths/weaknesses, XP reward. */
public class ResultPanel extends JPanel {

    private final MainFrame    frame;
    private final User         user;
    private final String       domain;
    private final String       mode;
    private final List<Question> questions;
    private final List<String>   answers;

    private final ResultService  rs = new ResultService();
    private final ProfileService ps = new ProfileService();

    public ResultPanel(MainFrame frame, User user, String domain, String mode,
                       List<Question> questions, List<String> answers) {
        this.frame     = frame;
        this.user      = user;
        this.domain    = domain;
        this.mode      = mode;
        this.questions = questions;
        this.answers   = answers;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
    }

    private void build() {
        int total   = questions.size();
        int correct = 0;
        for (int i = 0; i < total; i++) {
            String sel = (i < answers.size()) ? answers.get(i) : null;
            if (sel != null && sel.trim().equalsIgnoreCase(questions.get(i).getCorrectAnswer().trim()))
                correct++;
        }
        int percent = total == 0 ? 0 : (correct * 100) / total;
        int xpGained = (percent / 10) + Math.min(total, 20);

        // Save to DB (passing difficulty as well)
        String difficulty = "Mixed";
        if (questions != null && !questions.isEmpty()) {
            difficulty = questions.get(0).getDifficulty();
        }
        try {
            rs.saveResult(user.getUsername(), domain, percent, mode, correct, total, difficulty);
            ps.checkAndGrantMilestones(user.getUsername(),
                    rs.getTestCount(user.getUsername()), rs.getBestScore(user.getUsername()));
        } catch (Exception ex) { /* silent */ }

        // ── TOP: Score section ────────────────────────────────────────────────
        GradientPanel topPanel = new GradientPanel(new Color(30,41,59), new Color(15,23,42), false);
        topPanel.setLayout(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(0, 280));

        JPanel scoreContent = new JPanel();
        scoreContent.setLayout(new BoxLayout(scoreContent, BoxLayout.Y_AXIS));
        scoreContent.setOpaque(false);
        scoreContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        CircularScoreIndicator gauge = new CircularScoreIndicator(150);
        gauge.setScore(percent);
        gauge.setAlignmentX(Component.CENTER_ALIGNMENT);

        String grade = percent >= 90 ? "Excellent!" : percent >= 75 ? "Great Job!" :
                       percent >= 50 ? "Good Effort" : "Keep Practising";
        JLabel gradeLbl = new JLabel(grade);
        gradeLbl.setFont(Theme.TITLE_FONT);
        gradeLbl.setForeground(Color.WHITE);
        gradeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statsLbl = new JLabel(correct + " correct  ·  " + (total - correct) +
                " wrong  ·  " + total + " total  ·  +" + xpGained + " XP");
        statsLbl.setFont(Theme.BODY_FONT);
        statsLbl.setForeground(Theme.DARK_TEXT_SUB);
        statsLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel domainLbl = new JLabel(domain + "  ·  " + mode + " Mode");
        domainLbl.setFont(Theme.SMALL_FONT);
        domainLbl.setForeground(Theme.ACCENT);
        domainLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreContent.add(gauge);
        scoreContent.add(Box.createVerticalStrut(12));
        scoreContent.add(gradeLbl);
        scoreContent.add(Box.createVerticalStrut(6));
        scoreContent.add(statsLbl);
        scoreContent.add(Box.createVerticalStrut(4));
        scoreContent.add(domainLbl);
        topPanel.add(scoreContent);
        add(topPanel, BorderLayout.NORTH);

        // ── CENTER: Review + Strengths/Weaknesses ─────────────────────────────
        JPanel reviewArea = new JPanel();
        reviewArea.setLayout(new BoxLayout(reviewArea, BoxLayout.Y_AXIS));
        reviewArea.setBackground(Theme.DARK_BG);
        reviewArea.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Strengths / Weaknesses chips
        try {
            Map<String, Integer> domAvg = rs.getDomainAverages(user.getUsername());
            JPanel swRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            swRow.setOpaque(false);
            swRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (Map.Entry<String, Integer> e : domAvg.entrySet()) {
                String d = e.getKey(); int sc = e.getValue();
                Color c = sc >= 80 ? Theme.SUCCESS : sc < 50 ? Theme.DANGER : Theme.WARNING;
                String prefix = sc >= 80 ? "Expert: " : sc < 50 ? "Needs Review: " : "Competent: ";
                JLabel chip = new JLabel(prefix + d + " " + sc + "%");
                chip.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
                chip.setForeground(c);
                chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(c.darker(), 1, true),
                    new EmptyBorder(3, 8, 3, 8)));
                swRow.add(chip);
            }

            if (!domAvg.isEmpty()) {
                JLabel swTitle = new JLabel("Performance Overview");
                swTitle.setFont(Theme.SUBTITLE_FONT);
                swTitle.setForeground(Theme.DARK_TEXT_MAIN);
                swTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                reviewArea.add(swTitle);
                reviewArea.add(Box.createVerticalStrut(10));
                reviewArea.add(swRow);
                reviewArea.add(Box.createVerticalStrut(22));
            }
        } catch (Exception ignored) {}

        // Question review
        JLabel reviewTitle = new JLabel("Question Review");
        reviewTitle.setIcon(IconFactory.getIcon("list", 20, Theme.ACCENT));
        reviewTitle.setIconTextGap(8);
        reviewTitle.setFont(Theme.SUBTITLE_FONT);
        reviewTitle.setForeground(Theme.DARK_TEXT_MAIN);
        reviewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviewArea.add(reviewTitle);
        reviewArea.add(Box.createVerticalStrut(12));

        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            String sel = (i < answers.size()) ? answers.get(i) : null;
            boolean ok = sel != null && sel.trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
            reviewArea.add(buildReviewCard(i + 1, q, sel, ok));
            reviewArea.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(reviewArea);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // ── BOTTOM: Action buttons ────────────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        bottom.setBackground(new Color(15,23,42));

        ModernButton dashBtn = new ModernButton("Back to Dashboard");
        dashBtn.setIcon(IconFactory.getIcon("home", 16, Color.WHITE));
        dashBtn.setColors(new Color(51,65,85), new Color(71,85,105));
        dashBtn.addActionListener(e -> frame.goToDashboard(user));

        ModernButton retryBtn = new ModernButton("Retry");
        retryBtn.setIcon(IconFactory.getIcon("retry", 16, Color.WHITE));
        retryBtn.addActionListener(e -> frame.startQuiz(user, domain, mode, null));

        bottom.add(dashBtn);
        if (domain != null && !domain.contains("Exam") && !domain.contains("Test")) bottom.add(retryBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildReviewCard(int num, Question q, String sel, boolean ok) {
        RoundedPanel card = new RoundedPanel(12, Theme.DARK_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorderOverride(ok ? Theme.SUCCESS.darker() : Theme.DANGER.darker(), 1);

        // Header row
        JPanel hRow = new JPanel(new BorderLayout(8, 0));
        hRow.setOpaque(false);
        JLabel numLbl = new JLabel("Q" + num);
        numLbl.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        numLbl.setForeground(Theme.DARK_TEXT_SUB);
        JLabel status = new JLabel(ok ? "Correct" : "Wrong");
        status.setIcon(IconFactory.getIcon(ok ? "check" : "close", 14, ok ? Theme.SUCCESS : Theme.DANGER));
        status.setIconTextGap(6);
        status.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        status.setForeground(ok ? Theme.SUCCESS : Theme.DANGER);
        hRow.add(numLbl, BorderLayout.WEST);
        hRow.add(status, BorderLayout.EAST);

        // Question
        JLabel qLbl = new JLabel("<html><b>" + q.getQuestion() + "</b></html>");
        qLbl.setFont(Theme.BODY_FONT);
        qLbl.setForeground(Theme.DARK_TEXT_MAIN);

        // Answer details
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        JLabel yourAns = new JLabel("Your answer: " + (sel != null ? sel : "Not answered"));
        yourAns.setFont(Theme.SMALL_FONT);
        yourAns.setForeground(ok ? Theme.SUCCESS : Theme.DANGER);

        JLabel corrAns = new JLabel("Correct answer: " + q.getCorrectAnswer());
        corrAns.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        corrAns.setForeground(Theme.SUCCESS);

        details.add(yourAns);
        if (!ok) details.add(corrAns);

        // Explanation
        if (q.getExplanation() != null && !q.getExplanation().isBlank()) {
            JLabel expLbl = new JLabel("<html><i>Explanation: " + q.getExplanation() + "</i></html>");
            expLbl.setIcon(IconFactory.getIcon("warning", 14, Theme.ACCENT));
            expLbl.setIconTextGap(6);
            expLbl.setFont(Theme.SMALL_FONT);
            expLbl.setForeground(new Color(148, 163, 184));
            details.add(Box.createVerticalStrut(4));
            details.add(expLbl);
        }

        card.add(hRow,    BorderLayout.NORTH);
        card.add(qLbl,    BorderLayout.CENTER);
        card.add(details, BorderLayout.SOUTH);
        return card;
    }
}
