package ui;

import model.ResultRecord;
import model.User;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Analytics tab showing summary metrics (Level, XP, Streak, Tests, etc.),
 * domain average bar charts, and a detailed Recent Activity list.
 */
public class AnalyticsSubPanel extends JPanel {

    private User user;
    private final ResultService rs = new ResultService();
    private final JPanel chartsArea = new JPanel();
    private final JPanel summaryGrid = new JPanel(new GridLayout(2, 3, 16, 16));
    private final JPanel activityArea = new JPanel();

    public AnalyticsSubPanel(User user) {
        this.user = user;
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

        JLabel heading = new JLabel("Performance Analytics");
        heading.setIcon(IconFactory.getIcon("analytics", 24, Theme.ACCENT));
        heading.setIconTextGap(10);
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Detailed breakdown of your stats, domain averages, and recent activity.");
        sub.setFont(Theme.BODY_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(heading);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(24));

        // Stats summary cards
        summaryGrid.setOpaque(false);
        summaryGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        inner.add(summaryGrid);
        inner.add(Box.createVerticalStrut(28));

        // Domain averages
        inner.add(sectionLabel("analytics", "Domain-wise Averages"));
        inner.add(Box.createVerticalStrut(12));

        chartsArea.setLayout(new BoxLayout(chartsArea, BoxLayout.Y_AXIS));
        chartsArea.setOpaque(false);
        chartsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(chartsArea);
        inner.add(Box.createVerticalStrut(28));

        // Recent Activity
        inner.add(sectionLabel("history", "Recent Activity"));
        inner.add(Box.createVerticalStrut(12));

        activityArea.setLayout(new BoxLayout(activityArea, BoxLayout.Y_AXIS));
        activityArea.setOpaque(false);
        activityArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(activityArea);

        return inner;
    }

    public void refresh(User u) {
        this.user = u;
        summaryGrid.removeAll();
        chartsArea.removeAll();
        activityArea.removeAll();

        try {
            // Load stats from DB
            int tests = rs.getTestCount(u.getUsername());
            int best = rs.getBestScore(u.getUsername());
            int avg = rs.getAverageScore(u.getUsername());
            int xp = rs.getUserXP(u.getUsername());
            int level = rs.getUserLevel(u.getUsername());
            int streak = rs.getUserStreak(u.getUsername());

            // 6 summary cards
            summaryGrid.add(miniStat("list", "Total Tests", String.valueOf(tests), Theme.ACCENT));
            summaryGrid.add(miniStat("trophy", "Best Score", best + "%", Theme.SUCCESS));
            summaryGrid.add(miniStat("analytics", "Avg Score", avg + "%", Theme.WARNING));
            summaryGrid.add(miniStat("star", "Current Level", "Level " + level, Theme.SUCCESS));
            summaryGrid.add(miniStat("star", "Total XP", xp + " XP", Theme.ACCENT));
            summaryGrid.add(miniStat("timer", "Daily Streak", streak + " Days", Theme.DANGER));

            // Domain-wise bars
            Map<String, Integer> domainAvg = rs.getDomainAverages(u.getUsername());
            if (domainAvg.isEmpty()) {
                JLabel none = new JLabel("No quiz data yet. Complete some quizzes to see averages!");
                none.setForeground(Theme.DARK_TEXT_SUB);
                none.setFont(Theme.BODY_FONT);
                chartsArea.add(none);
            } else {
                for (Map.Entry<String, Integer> entry : domainAvg.entrySet()) {
                    chartsArea.add(buildBar(entry.getKey(), entry.getValue()));
                    chartsArea.add(Box.createVerticalStrut(8));
                }
            }

            // Recent activity cards
            List<ResultRecord> recent = rs.getPreviousResults(u.getUsername());
            if (recent.isEmpty()) {
                JLabel none = new JLabel("No recent activity found.");
                none.setForeground(Theme.DARK_TEXT_SUB);
                none.setFont(Theme.BODY_FONT);
                activityArea.add(none);
            } else {
                SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                // Show up to 5 recent tests in Analytics
                int count = Math.min(5, recent.size());
                for (int i = 0; i < count; i++) {
                    activityArea.add(buildActivityCard(recent.get(i), df));
                    activityArea.add(Box.createVerticalStrut(8));
                }
            }

        } catch (Exception ex) {
            JLabel err = new JLabel("Could not load analytics: " + ex.getMessage());
            err.setForeground(Theme.DANGER);
            chartsArea.add(err);
        }

        summaryGrid.revalidate(); summaryGrid.repaint();
        chartsArea.revalidate(); chartsArea.repaint();
        activityArea.revalidate(); activityArea.repaint();
    }

    private JPanel buildBar(String domain, int score) {
        RoundedPanel row = new RoundedPanel(10, Theme.DARK_CARD);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(domain);
        nameLbl.setFont(Theme.BODY_FONT);
        nameLbl.setForeground(Theme.DARK_TEXT_MAIN);
        nameLbl.setPreferredSize(new Dimension(240, 0));

        Color barColor = score >= 80 ? Theme.SUCCESS : score >= 50 ? Theme.WARNING : Theme.DANGER;

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(51, 65, 85));
                g2.fillRoundRect(0, getHeight() / 2 - 5, getWidth(), 10, 10, 10);
                int fillW = (int) (getWidth() * score / 100.0);
                g2.setColor(barColor);
                g2.fillRoundRect(0, getHeight() / 2 - 5, fillW, 10, 10, 10);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 36));

        JLabel scoreLbl = new JLabel(score + "%");
        scoreLbl.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        scoreLbl.setForeground(barColor);
        scoreLbl.setPreferredSize(new Dimension(48, 0));
        scoreLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(scoreLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildActivityCard(ResultRecord r, SimpleDateFormat df) {
        RoundedPanel card = new RoundedPanel(10, Theme.DARK_CARD);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left info
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        
        JLabel topic = new JLabel(r.getTopic());
        topic.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        topic.setForeground(Theme.DARK_TEXT_MAIN);

        JLabel sub = new JLabel(r.getMode() + " Mode  ·  " + (r.getTestDate() != null ? df.format(r.getTestDate()) : "—"));
        sub.setFont(Theme.SMALL_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);

        left.add(topic);
        left.add(sub);

        // Right score
        int score = r.getScorePercent();
        Color scoreColor = score >= 80 ? Theme.SUCCESS : score >= 50 ? Theme.WARNING : Theme.DANGER;
        JLabel scoreLbl = new JLabel(score + "%");
        scoreLbl.setFont(Theme.SUBTITLE_FONT.deriveFont(Font.BOLD));
        scoreLbl.setForeground(scoreColor);
        scoreLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(left, BorderLayout.WEST);
        card.add(scoreLbl, BorderLayout.EAST);
        return card;
    }

    private JPanel miniStat(String iconKey, String title, String value, Color accent) {
        RoundedPanel p = new RoundedPanel(12, Theme.DARK_CARD);
        p.setLayout(new BorderLayout(8, 4));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.setBorderOverride(accent.darker(), 1);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel icon = new JLabel(IconFactory.getIcon(iconKey, 14, accent));
        JLabel tl = new JLabel("  " + title);
        tl.setFont(Theme.SMALL_FONT);
        tl.setForeground(Theme.DARK_TEXT_SUB);
        top.add(icon); top.add(tl);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 20));
        v.setForeground(Color.WHITE);

        p.add(top, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
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
}
