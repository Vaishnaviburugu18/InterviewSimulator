package ui;

import model.User;
import service.QuizService;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/** Home tab: stats cards, quick-start, recent activity. */
public class HomeSubPanel extends JPanel {

    private final MainFrame frame;
    private User user;
    private final ResultService rs = new ResultService();
    private final JPanel statsRow  = new JPanel(new GridLayout(1, 4, 16, 0));
    private final JPanel recentBox = new JPanel();

    public HomeSubPanel(MainFrame frame, User user) {
        this.frame = frame;
        this.user  = user;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
        if (user != null) refresh(user);
    }

    private void build() {
        JScrollPane scroll = new JScrollPane(buildInner());
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 8));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildInner() {
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(Theme.DARK_BG);
        inner.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel welcome = new JLabel(user != null ? "Good day, " + user.getUsername() + "!" : "Welcome!");
        welcome.setFont(Theme.TITLE_FONT);
        welcome.setForeground(Theme.DARK_TEXT_MAIN);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Track your progress and keep practicing to crack your dream company.");
        sub.setFont(Theme.BODY_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(welcome);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(24));

        // Stats row
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        inner.add(statsRow);
        inner.add(Box.createVerticalStrut(28));

        // Quick-start section
        JLabel qs = sectionLabel("Quick Start");
        inner.add(qs);
        inner.add(Box.createVerticalStrut(12));

        JPanel modeGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        modeGrid.setOpaque(false);
        modeGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        addModeCard(modeGrid, "book", "Practice Mode",
                "No pressure. Instant feedback.", new Color(67, 56, 202), "Practice");
        addModeCard(modeGrid, "timer", "Timed Test",
                "30-second timer per question.", new Color(5, 150, 105), "Timed");
        addModeCard(modeGrid, "trophy", "Mock Interview",
                "10-question simulation.", new Color(217, 119, 6), "Mock");
        addModeCard(modeGrid, "user", "Company Specific",
                "TCS · Infosys · UBS · more.", new Color(139, 92, 246), "Company");

        inner.add(modeGrid);
        inner.add(Box.createVerticalStrut(28));

        // Recent activity
        JLabel ra = sectionLabel("Recent Activity");
        inner.add(ra);
        inner.add(Box.createVerticalStrut(12));

        recentBox.setLayout(new BoxLayout(recentBox, BoxLayout.Y_AXIS));
        recentBox.setOpaque(false);
        recentBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(recentBox);

        return inner;
    }

    private void addModeCard(JPanel grid, String iconKey, String title, String desc, Color accent, String mode) {
        RoundedPanel card = new RoundedPanel(14, Theme.DARK_CARD);
        card.setLayout(new BorderLayout(12, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorderOverride(accent.darker(), 1);

        JLabel iconLbl = new JLabel(IconFactory.getIcon(iconKey, 24, Color.WHITE));
        
        JLabel t = new JLabel(title);
        t.setFont(Theme.CARD_TITLE_FONT);
        t.setForeground(Color.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.add(iconLbl);
        header.add(Box.createHorizontalStrut(10));
        header.add(t);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(Theme.SMALL_FONT);
        d.setForeground(Theme.DARK_TEXT_SUB);

        card.add(header, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);

        // Hover tint
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBgOverride(accent.darker().darker());
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBgOverride(Theme.DARK_CARD);
                card.repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (user == null) return;
                if (mode.equals("Company")) {
                    String choice = ModernSelectionDialog.showCompanySelection(frame, "Select Company", "Choose a company for the assessment");
                    if (choice != null) frame.startQuiz(user, null, "Company", choice);
                } else {
                    String domain = ModernSelectionDialog.showDomainSelection(frame, "Select Domain", "Choose a practice domain");
                    if (domain != null) frame.startQuiz(user, domain, mode, null);
                }
            }
        });

        grid.add(card);
    }

    public void refresh(User u) {
        this.user = u;
        statsRow.removeAll();
        recentBox.removeAll();

        try {
            int tests   = rs.getTestCount(u.getUsername());
            int best    = rs.getBestScore(u.getUsername());
            int avg     = rs.getAverageScore(u.getUsername());
            int streak  = rs.getUserStreak(u.getUsername());
            int xp      = rs.getUserXP(u.getUsername());
            int level   = rs.getUserLevel(u.getUsername());

            statsRow.add(statCard("list", "Tests Taken", String.valueOf(tests), new Color(99, 102, 241)));
            statsRow.add(statCard("trophy", "Best Score",  best + "%",            new Color(16, 185, 129)));
            statsRow.add(statCard("analytics", "Average",      avg + "%",             new Color(245, 158, 11)));
            statsRow.add(statCard("star", "XP / Level",   xp + " / Lv." + level, new Color(139, 92, 246)));

            // Recent 5 results
            var records = rs.getPreviousResults(u.getUsername());
            if (records.isEmpty()) {
                JLabel none = new JLabel("No quiz history yet. Start a quiz to see your results here!");
                none.setFont(Theme.BODY_FONT);
                none.setForeground(Theme.DARK_TEXT_SUB);
                recentBox.add(none);
            } else {
                int limit = Math.min(5, records.size());
                for (int i = 0; i < limit; i++) {
                    var r = records.get(i);
                    recentBox.add(activityRow(r.getTopic(), r.getScorePercent(),
                             r.getMode(), r.getTestDate()));
                    recentBox.add(Box.createVerticalStrut(8));
                }
            }
        } catch (Exception ex) {
            statsRow.add(statCard("danger", "Error", "DB", Color.RED));
        }

        statsRow.revalidate(); statsRow.repaint();
        recentBox.revalidate(); recentBox.repaint();
    }

    private JPanel statCard(String iconKey, String title, String value, Color accent) {
        RoundedPanel p = new RoundedPanel(14, Theme.DARK_CARD);
        p.setLayout(new BorderLayout(8, 6));
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        p.setBorderOverride(accent.darker(), 1);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel icon = new JLabel(IconFactory.getIcon(iconKey, 14, accent));
        JLabel tl = new JLabel("  " + title);
        tl.setFont(Theme.SMALL_FONT);
        tl.setForeground(Theme.DARK_TEXT_SUB);
        top.add(icon); top.add(tl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 26));
        val.setForeground(Color.WHITE);

        p.add(top, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JPanel activityRow(String topic, int score, String mode, java.sql.Timestamp date) {
        RoundedPanel row = new RoundedPanel(10, Theme.DARK_CARD);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(10, 16, 10, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        Color scoreColor = score >= 80 ? Theme.SUCCESS : score >= 50 ? Theme.WARNING : Theme.DANGER;
        JLabel scoreLbl = new JLabel(score + "%");
        scoreLbl.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        scoreLbl.setForeground(scoreColor);
        scoreLbl.setPreferredSize(new Dimension(50, 0));

        JPanel mid = new JPanel(new GridLayout(2, 1));
        mid.setOpaque(false);

        // Vector icon for the domain
        String iconKey = getIconKeyForDomain(topic);
        Icon domIcon = IconFactory.getIcon(iconKey, 16, Theme.ACCENT);
        
        JPanel topicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topicPanel.setOpaque(false);
        topicPanel.add(new JLabel(domIcon));
        JLabel topicL = new JLabel("  " + topic);
        topicL.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        topicL.setForeground(Theme.DARK_TEXT_MAIN);
        topicPanel.add(topicL);

        JLabel modeL = new JLabel(mode != null ? mode + " Mode" : "Practice Mode");
        modeL.setFont(Theme.SMALL_FONT);
        modeL.setForeground(Theme.DARK_TEXT_SUB);
        
        mid.add(topicPanel); mid.add(modeL);

        JLabel dateLbl = new JLabel(date != null ?
                new java.text.SimpleDateFormat("dd MMM").format(date) : "");
        dateLbl.setFont(Theme.SMALL_FONT);
        dateLbl.setForeground(Theme.DARK_TEXT_SUB);

        row.add(scoreLbl, BorderLayout.WEST);
        row.add(mid, BorderLayout.CENTER);
        row.add(dateLbl, BorderLayout.EAST);
        return row;
    }

    private String getIconKeyForDomain(String domain) {
        String lower = domain.toLowerCase();
        if (lower.contains("java") && !lower.contains("javascript")) return "gear";
        if (lower.contains("python") || lower.contains("c++") || lower.contains("c programming")) return "settings";
        if (lower.contains("html") || lower.contains("css") || lower.contains("web")) return "book";
        if (lower.contains("sql") || lower.contains("dbms") || lower.contains("database")) return "book";
        if (lower.contains("network")) return "settings";
        if (lower.contains("operating")) return "settings";
        if (lower.contains("devops")) return "logout";
        if (lower.contains("security")) return "lock";
        if (lower.contains("analytics") || lower.contains("data science") || lower.contains("performance")) return "analytics";
        if (lower.contains("aptitude") || lower.contains("reasoning")) return "list";
        return "book";
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SUBTITLE_FONT);
        l.setForeground(Theme.DARK_TEXT_MAIN);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
