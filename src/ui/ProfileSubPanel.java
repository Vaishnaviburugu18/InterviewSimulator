package ui;

import model.User;
import service.ProfileService;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Set;

/** Profile tab: stats, badges, favourite domains, resume recommender, and password update. */
public class ProfileSubPanel extends JPanel {

    private final MainFrame frame;
    private User user;
    private final ResultService  rs = new ResultService();
    private final ProfileService ps = new ProfileService();

    private final JLabel headerUserLabel = new JLabel(" ");
    private final JLabel headerEmailLabel = new JLabel(" ");

    private final JPanel badgeBox   = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
    private final JPanel favBox     = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
    private final JPanel statsBox   = new JPanel(new GridLayout(2, 3, 14, 14));
    
    private final JTextArea resumeArea = new JTextArea(5, 30);
    private final JPanel  recsBox   = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

    // Password Update Fields
    private final JPasswordField oldPassword = new JPasswordField(20);
    private final JPasswordField newPassword = new JPasswordField(20);
    private final JPasswordField confirmPassword = new JPasswordField(20);
    private final JLabel passwordStatus = new JLabel(" ");

    public ProfileSubPanel(MainFrame frame, User user) {
        this.frame = frame;
        this.user  = user;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
        if (user != null) refresh(user);
    }

    private void build() {
        JScrollPane scroll = new JScrollPane(buildInner());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildInner() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Theme.DARK_BG);
        inner.setBorder(new EmptyBorder(28, 28, 28, 28));

        // ── Heading & Profile Header Card ─────────────────────────────────────
        JLabel heading = new JLabel("My Profile");
        heading.setIcon(IconFactory.getIcon("user", 24, Theme.ACCENT));
        heading.setIconTextGap(10);
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(heading);
        inner.add(Box.createVerticalStrut(16));

        // Profile details header card
        RoundedPanel headerCard = new RoundedPanel(16, Theme.SIDEBAR_BG);
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        headerUserLabel.setFont(Theme.SUBTITLE_FONT);
        headerUserLabel.setForeground(Color.WHITE);
        
        headerEmailLabel.setFont(Theme.BODY_FONT);
        headerEmailLabel.setForeground(Theme.DARK_TEXT_SUB);

        detailsPanel.add(headerUserLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(headerEmailLabel);

        JLabel userAvatar = new JLabel();
        userAvatar.setIcon(IconFactory.getIcon("user", 42, Theme.ACCENT));

        headerCard.add(userAvatar, BorderLayout.WEST);
        headerCard.add(detailsPanel, BorderLayout.CENTER);
        inner.add(headerCard);
        inner.add(Box.createVerticalStrut(28));

        // ── Stats ─────────────────────────────────────────────────────────────
        inner.add(sectionLabel("analytics", "Statistics"));
        inner.add(Box.createVerticalStrut(12));
        statsBox.setOpaque(false);
        statsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        inner.add(statsBox);
        inner.add(Box.createVerticalStrut(28));

        // ── Badges ───────────────────────────────────────────────────────────
        inner.add(sectionLabel("trophy", "Achievements & Badges"));
        inner.add(Box.createVerticalStrut(10));
        badgeBox.setOpaque(false);
        badgeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(badgeBox);
        inner.add(Box.createVerticalStrut(28));

        // ── Favourite domains ─────────────────────────────────────────────────
        inner.add(sectionLabel("star", "Favourite Domains"));
        inner.add(Box.createVerticalStrut(10));
        favBox.setOpaque(false);
        favBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(favBox);
        inner.add(Box.createVerticalStrut(28));

        // ── Resume recommender ────────────────────────────────────────────────
        inner.add(sectionLabel("book", "Resume-Based Domain Recommender"));
        inner.add(Box.createVerticalStrut(6));

        JLabel hint = new JLabel("Paste keywords or a snippet from your resume to get domain suggestions.");
        hint.setFont(Theme.SMALL_FONT);
        hint.setForeground(Theme.DARK_TEXT_SUB);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(hint);
        inner.add(Box.createVerticalStrut(10));

        resumeArea.setFont(Theme.CODE_FONT);
        resumeArea.setBackground(Theme.DARK_CARD);
        resumeArea.setForeground(Theme.DARK_TEXT_MAIN);
        resumeArea.setCaretColor(Theme.DARK_TEXT_MAIN);
        resumeArea.setLineWrap(true);
        resumeArea.setWrapStyleWord(true);
        resumeArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        JScrollPane rScroll = new JScrollPane(resumeArea);
        rScroll.setBorder(BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true));
        rScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        inner.add(rScroll);
        inner.add(Box.createVerticalStrut(10));

        ModernButton analyzeBtn = new ModernButton("Analyse Resume");
        analyzeBtn.setIcon(IconFactory.getIcon("search", 14, Color.WHITE));
        analyzeBtn.setIconTextGap(8);
        analyzeBtn.setMaximumSize(new Dimension(200, 36));
        analyzeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        analyzeBtn.addActionListener(e -> analyzeResume());
        inner.add(analyzeBtn);
        inner.add(Box.createVerticalStrut(12));

        recsBox.setOpaque(false);
        recsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(recsBox);
        inner.add(Box.createVerticalStrut(28));

        // ── Password Update Section ──────────────────────────────────────────
        inner.add(sectionLabel("lock", "Security & Password Update"));
        inner.add(Box.createVerticalStrut(12));

        RoundedPanel pwdCard = new RoundedPanel(16, Theme.DARK_CARD);
        pwdCard.setLayout(new GridBagLayout());
        pwdCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        pwdCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0;
        
        pwdCard.add(label("Old Password:"), g);
        g.gridx = 1; pwdCard.add(oldPassword, g);

        g.gridx = 0; g.gridy = 1;
        pwdCard.add(label("New Password:"), g);
        g.gridx = 1; pwdCard.add(newPassword, g);

        g.gridx = 0; g.gridy = 2;
        pwdCard.add(label("Confirm Password:"), g);
        g.gridx = 1; pwdCard.add(confirmPassword, g);

        // Status label
        passwordStatus.setFont(Theme.SMALL_FONT);
        passwordStatus.setForeground(Theme.SUCCESS);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        pwdCard.add(passwordStatus, g);

        // Update button
        ModernButton updatePwdBtn = new ModernButton("Update Password");
        updatePwdBtn.setPreferredSize(new Dimension(150, 32));
        updatePwdBtn.addActionListener(e -> doUpdatePassword());
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        pwdCard.add(updatePwdBtn, g);

        stylePassField(oldPassword);
        stylePassField(newPassword);
        stylePassField(confirmPassword);

        inner.add(pwdCard);

        return inner;
    }

    public void refresh(User u) {
        this.user = u;
        headerUserLabel.setText(u.getUsername());
        headerEmailLabel.setText(u.getEmail());

        statsBox.removeAll();
        badgeBox.removeAll();
        favBox.removeAll();

        try {
            int tests  = rs.getTestCount(u.getUsername());
            int best   = rs.getBestScore(u.getUsername());
            int avg    = rs.getAverageScore(u.getUsername());
            int xp     = rs.getUserXP(u.getUsername());
            int level  = rs.getUserLevel(u.getUsername());
            int streak = rs.getUserStreak(u.getUsername());

            statsBox.add(statCard("list", "Tests Taken",  String.valueOf(tests),  Theme.ACCENT));
            statsBox.add(statCard("trophy", "Best Score",   best + "%",             Theme.SUCCESS));
            statsBox.add(statCard("analytics", "Average",       avg + "%",              Theme.WARNING));
            statsBox.add(statCard("star", "XP Points",     String.valueOf(xp),     Theme.ACCENT));
            statsBox.add(statCard("star", "Level",         String.valueOf(level),  Theme.SUCCESS));
            statsBox.add(statCard("timer", "Streak",        streak + " days",       Theme.DANGER));

            // Auto-grant milestones
            ps.checkAndGrantMilestones(u.getUsername(), tests, best);

            // Badges
            List<String> badges = ps.getAchievements(u.getUsername());
            if (badges.isEmpty()) {
                JLabel none = pill(null, "Complete quizzes to unlock badges!", Theme.DARK_TEXT_SUB);
                badgeBox.add(none);
            } else {
                for (String b : badges) badgeBox.add(badgePill(b));
            }

            // Favourites
            Set<String> favs = ps.getFavoriteDomains(u.getUsername());
            if (favs.isEmpty()) {
                JLabel none = pill(null, "Star domains in the Domains tab to see them here.", Theme.DARK_TEXT_SUB);
                favBox.add(none);
            } else {
                for (String f : favs) favBox.add(pill("star", f, Theme.WARNING));
            }

        } catch (Exception ex) {
            statsBox.add(new JLabel("DB error: " + ex.getMessage()));
        }

        statsBox.revalidate(); statsBox.repaint();
        badgeBox.revalidate(); badgeBox.repaint();
        favBox.revalidate();   favBox.repaint();
    }

    private void doUpdatePassword() {
        String oldPwd = new String(oldPassword.getPassword());
        String newPwd = new String(newPassword.getPassword());
        String confPwd = new String(confirmPassword.getPassword());

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confPwd.isEmpty()) {
            passwordStatus.setForeground(Theme.DANGER);
            passwordStatus.setText("All fields are required.");
            return;
        }

        if (!newPwd.equals(confPwd)) {
            passwordStatus.setForeground(Theme.DANGER);
            passwordStatus.setText("New passwords do not match.");
            return;
        }

        try {
            ps.updatePassword(user.getUsername(), oldPwd, newPwd);
            passwordStatus.setForeground(Theme.SUCCESS);
            passwordStatus.setText("Password updated successfully!");
            oldPassword.setText("");
            newPassword.setText("");
            confirmPassword.setText("");
        } catch (Exception ex) {
            passwordStatus.setForeground(Theme.DANGER);
            passwordStatus.setText(ex.getMessage());
        }
    }

    private void analyzeResume() {
        recsBox.removeAll();
        String text = resumeArea.getText();
        List<String> recs = ps.recommendDomains(text);
        if (recs.isEmpty()) {
            recsBox.add(pill(null, "No matching domains found. Try adding more skills.", Theme.DARK_TEXT_SUB));
        } else {
            JLabel intro = pill(null, "Recommended Domains: ", Theme.DARK_TEXT_MAIN);
            recsBox.add(intro);
            for (String r : recs) recsBox.add(pill("book", r, Theme.ACCENT));
        }
        recsBox.revalidate(); recsBox.repaint();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel statCard(String iconKey, String title, String value, Color accent) {
        RoundedPanel p = new RoundedPanel(12, Theme.DARK_CARD);
        p.setLayout(new BorderLayout(8, 4));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        p.setBorderOverride(accent.darker(), 1);
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel icon = new JLabel(IconFactory.getIcon(iconKey, 14, accent));
        JLabel t = new JLabel("  " + title);
        t.setFont(Theme.SMALL_FONT);
        t.setForeground(Theme.DARK_TEXT_SUB);
        top.add(icon); top.add(t);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(Color.WHITE);
        p.add(top, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JLabel badgePill(String text) {
        JLabel l = new JLabel(text);
        l.setIcon(IconFactory.getIcon("trophy", 12, Theme.WARNING));
        l.setIconTextGap(6);
        l.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        l.setForeground(Theme.WARNING);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.WARNING.darker(), 1, true),
            new EmptyBorder(4, 10, 4, 10)));
        return l;
    }

    private JLabel pill(String iconKey, String text, Color fg) {
        JLabel l = new JLabel(text);
        if (iconKey != null) {
            l.setIcon(IconFactory.getIcon(iconKey, 12, fg));
            l.setIconTextGap(6);
        }
        l.setFont(Theme.SMALL_FONT);
        l.setForeground(fg);
        l.setBorder(new EmptyBorder(2, 6, 2, 6));
        return l;
    }

    private JLabel sectionLabel(String iconKey, String text) {
        JLabel l = new JLabel(text);
        l.setIcon(IconFactory.getIcon(iconKey, 18, Theme.ACCENT));
        l.setIconTextGap(8);
        l.setFont(Theme.SUBTITLE_FONT);
        l.setForeground(Theme.DARK_TEXT_MAIN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        l.setForeground(Theme.DARK_TEXT_SUB);
        return l;
    }

    private void stylePassField(JPasswordField f) {
        f.setFont(Theme.BODY_FONT);
        f.setBackground(Theme.DARK_BG);
        f.setForeground(Theme.DARK_TEXT_MAIN);
        f.setCaretColor(Theme.DARK_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
    }
}
