package ui;

import model.User;
import service.ProfileService;
import service.QuizService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

/** Domains tab: searchable grid of all 30 domains with favorite toggle. */
public class DomainSubPanel extends JPanel {

    private final MainFrame frame;
    private User user;
    private final ProfileService profileService = new ProfileService();

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> diffBox = new JComboBox<>(
            new String[]{"All Levels", "Beginner", "Intermediate", "Advanced"});
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
    private Set<String> favorites = new java.util.HashSet<>();

    public DomainSubPanel(MainFrame frame, User user) {
        this.frame = frame;
        this.user  = user;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
        refresh(user);
    }

    private void build() {
        // Top filter bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(24, 28, 16, 28));

        JLabel heading = new JLabel("All Domains");
        heading.setIcon(IconFactory.getIcon("book", 24, Theme.ACCENT));
        heading.setIconTextGap(10);
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filters.setOpaque(false);

        styleField(searchField);
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.setToolTipText("Search domains...");

        diffBox.setFont(Theme.SMALL_FONT);
        diffBox.setBackground(Theme.DARK_CARD);
        diffBox.setForeground(Theme.DARK_TEXT_MAIN);
        diffBox.setPreferredSize(new Dimension(150, 36));

        filters.add(new JLabel() {{ setText("Filter:"); setForeground(Theme.DARK_TEXT_SUB); setFont(Theme.SMALL_FONT); }});
        filters.add(diffBox);
        filters.add(searchField);

        topBar.add(heading, BorderLayout.WEST);
        topBar.add(filters, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Scrollable grid
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 28, 28, 28));
        JScrollPane scroll = new JScrollPane(grid);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 8));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Search / filter listeners
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { rebuildGrid(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { rebuildGrid(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { rebuildGrid(); }
        });
        diffBox.addActionListener(e -> rebuildGrid());
    }

    public void refresh(User u) {
        this.user = u;
        try {
            favorites = profileService.getFavoriteDomains(u.getUsername());
        } catch (Exception ex) {
            favorites = new java.util.HashSet<>();
        }
        rebuildGrid();
    }

    private void rebuildGrid() {
        grid.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        String diff  = (String) diffBox.getSelectedItem();

        for (String domain : QuizService.ALL_DOMAINS) {
            if (!query.isEmpty() && !domain.toLowerCase().contains(query)) continue;
            grid.add(buildDomainCard(domain, diff));
        }

        if (grid.getComponentCount() == 0) {
            JLabel none = new JLabel("No domains match your search.");
            none.setForeground(Theme.DARK_TEXT_SUB);
            none.setFont(Theme.BODY_FONT);
            grid.add(none);
        }

        grid.revalidate();
        grid.repaint();
    }

    private JPanel buildDomainCard(String domain, String selectedDiff) {
        boolean isFav = favorites.contains(domain);
        RoundedPanel card = new RoundedPanel(14, Theme.DARK_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(18, 18, 16, 18));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon + title
        JLabel icon = new JLabel(IconFactory.getIcon(getIconKeyForDomain(domain), 28, Theme.ACCENT));

        JLabel name = new JLabel("<html><b>" + domain + "</b></html>");
        name.setFont(Theme.CARD_TITLE_FONT);
        name.setForeground(Theme.DARK_TEXT_MAIN);

        // Difficulty chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        chips.setOpaque(false);
        for (String d : new String[]{"Beginner", "Intermediate", "Advanced"}) {
            JLabel chip = new JLabel(d);
            chip.setFont(new Font("Segoe UI", Font.BOLD, 10));
            Color c = d.equals("Beginner") ? Theme.SUCCESS :
                      d.equals("Intermediate") ? Theme.WARNING : Theme.DANGER;
            chip.setForeground(c);
            chip.setBorder(BorderFactory.createLineBorder(c.darker(), 1, true));
            chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c.darker(), 1, true),
                new EmptyBorder(2, 6, 2, 6)));
            chips.add(chip);
        }

        // Action buttons row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);

        ModernButton startBtn = new ModernButton("Start");
        startBtn.setIcon(IconFactory.getIcon("arrow-right", 12, Color.WHITE));
        startBtn.setIconTextGap(6);
        startBtn.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        startBtn.setPreferredSize(new Dimension(85, 30));
        startBtn.setCornerRadius(8);

        JToggleButton favBtn = new JToggleButton();
        favBtn.setSelected(isFav);
        favBtn.setIcon(IconFactory.getIcon(isFav ? "star" : "star-empty", 16, isFav ? Theme.WARNING : Theme.DARK_TEXT_SUB));
        favBtn.setContentAreaFilled(false);
        favBtn.setBorderPainted(false);
        favBtn.setFocusPainted(false);
        favBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        favBtn.setPreferredSize(new Dimension(34, 30));

        favBtn.addActionListener(e -> {
            boolean nowFav = favBtn.isSelected();
            favBtn.setIcon(IconFactory.getIcon(nowFav ? "star" : "star-empty", 16, nowFav ? Theme.WARNING : Theme.DARK_TEXT_SUB));
            try {
                if (nowFav) { profileService.addFavoriteDomain(user.getUsername(), domain); favorites.add(domain); }
                else        { profileService.removeFavoriteDomain(user.getUsername(), domain); favorites.remove(domain); }
            } catch (Exception ex) { /* silent */ }
        });

        startBtn.addActionListener(e -> {
            if (user == null) return;
            String mode = ModernSelectionDialog.showModeSelection(frame, "Select Quiz Mode", "Choose how you want to attempt: " + domain);
            if (mode != null) frame.startQuiz(user, domain, mode, null);
        });

        actions.add(startBtn);
        actions.add(favBtn);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        top.add(icon, BorderLayout.WEST);
        top.add(name, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(chips, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBorderOverride(Theme.ACCENT, 1); card.repaint(); }
            public void mouseExited(MouseEvent e)  { card.setBorderOverride(null, 0); card.repaint(); }
        });

        return card;
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

    private void styleField(JTextField f) {
        f.setFont(Theme.SMALL_FONT);
        f.setBackground(Theme.DARK_CARD);
        f.setForeground(Theme.DARK_TEXT_MAIN);
        f.setCaretColor(Theme.DARK_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)));
    }
}
