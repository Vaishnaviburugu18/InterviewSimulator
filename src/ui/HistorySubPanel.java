package ui;

import model.ResultRecord;
import model.User;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/** History tab: scrollable table of all past quiz attempts with details. */
public class HistorySubPanel extends JPanel {

    private User user;
    private final ResultService rs = new ResultService();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public HistorySubPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);

        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Domain", "Mode", "Difficulty", "Score", "Accuracy", "XP Earned"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();
        build();
        if (user != null) refresh(user);
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JLabel heading = new JLabel("Quiz History");
        heading.setIcon(IconFactory.getIcon("history", 24, Theme.ACCENT));
        heading.setIconTextGap(10);
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);

        JLabel sub = new JLabel("Your last 100 quiz attempts containing details of your performance.");
        sub.setFont(Theme.BODY_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);

        JPanel hInner = new JPanel();
        hInner.setLayout(new BoxLayout(hInner, BoxLayout.Y_AXIS));
        hInner.setOpaque(false);
        hInner.add(heading); hInner.add(Box.createVerticalStrut(4)); hInner.add(sub);
        header.add(hInner, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 8));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        scroll.getViewport().setBackground(Theme.DARK_CARD);
        scroll.setBackground(Theme.DARK_BG);
        add(scroll, BorderLayout.CENTER);
    }

    private void styleTable() {
        table.setBackground(Theme.DARK_CARD);
        table.setForeground(Theme.DARK_TEXT_MAIN);
        table.setFont(Theme.BODY_FONT);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(99, 102, 241, 80));
        table.setSelectionForeground(Color.WHITE);
        table.setFocusable(false);

        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(15, 23, 42));
        th.setForeground(Theme.DARK_TEXT_SUB);
        th.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.DARK_BORDER));
        th.setPreferredSize(new Dimension(0, 36));

        // Accuracy column renderer (index 5, color-coded)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setBackground(sel ? new Color(99,102,241,80) : Theme.DARK_CARD);
                if (v != null) {
                    String s = v.toString().replace("%", "");
                    try {
                        int score = Integer.parseInt(s.trim());
                        setForeground(score >= 80 ? Theme.SUCCESS : score >= 50 ? Theme.WARNING : Theme.DANGER);
                    } catch (NumberFormatException e) { setForeground(Theme.DARK_TEXT_MAIN); }
                }
                return this;
            }
        });

        // Default cell renderer for other cols
        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? new Color(99,102,241,80) : Theme.DARK_CARD);
                setForeground(Theme.DARK_TEXT_MAIN);
                setFont(Theme.BODY_FONT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (col == 0 || col >= 3) {
                    setHorizontalAlignment(CENTER);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 5) table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    public void refresh(User u) {
        this.user = u;
        tableModel.setRowCount(0);
        try {
            List<ResultRecord> records = rs.getPreviousResults(u.getUsername());
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy  HH:mm");
            if (records.isEmpty()) {
                tableModel.addRow(new Object[]{"—", "No history yet", "—", "—", "—", "—", "—"});
            } else {
                for (ResultRecord r : records) {
                    int xpGained = (r.getScorePercent() / 10) + Math.min(r.getTotalCount(), 20);
                    tableModel.addRow(new Object[]{
                        r.getTestDate() != null ? df.format(r.getTestDate()) : "—",
                        r.getTopic(),
                        r.getMode() != null ? r.getMode() : "Practice",
                        r.getDifficulty() != null ? r.getDifficulty() : "Mixed",
                        r.getCorrectCount() + " / " + r.getTotalCount(),
                        r.getScorePercent() + "%",
                        "+" + xpGained + " XP"
                    });
                }
            }
        } catch (Exception ex) {
            tableModel.addRow(new Object[]{"Error", ex.getMessage(), "", "", "", "", ""});
        }
    }
}
