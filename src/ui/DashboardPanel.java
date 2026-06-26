package ui;

import model.User;
import service.QuizService;
import service.ResultService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main application shell after login.
 * Contains a left sidebar + a right content area (CardLayout).
 * Tabs: Home | Domains | Analytics | History | Profile | Admin
 */
public class DashboardPanel extends JPanel {

    public static final String HOME      = "HOME";
    public static final String DOMAINS   = "DOMAINS";
    public static final String ANALYTICS = "ANALYTICS";
    public static final String HISTORY   = "HISTORY";
    public static final String PROFILE   = "PROFILE";

    private final MainFrame frame;
    private User user;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentArea   = new JPanel(contentLayout);

    // Sub-panels
    private HomeSubPanel      homePanel;
    private DomainSubPanel    domainPanel;
    private AnalyticsSubPanel analyticsPanel;
    private HistorySubPanel   historyPanel;
    private ProfileSubPanel   profilePanel;

    // Sidebar buttons
    private final List<SidebarBtn> sidebarBtns = new ArrayList<>();
    private final JLabel userNameLabel  = new JLabel();
    private final JLabel userLevelLabel = new JLabel();
    private final JProgressBar xpBar   = new JProgressBar();

    private final ResultService resultService = new ResultService();

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        buildSidebar();
        buildContentArea();
    }

    public void setUser(User u) {
        this.user = u;
        refresh(u);
    }

    public void refresh(User u) {
        this.user = u;
        userNameLabel.setText(u.getUsername());
        try {
            int xp    = resultService.getUserXP(u.getUsername());
            int level = resultService.getUserLevel(u.getUsername());
            userLevelLabel.setText("Level " + level + "  ·  " + xp + " XP");
            xpBar.setValue(xp % 100);
        } catch (Exception ex) {
            userLevelLabel.setText("Level 1");
        }

        if (homePanel != null)      homePanel.refresh(u);
        if (domainPanel != null)    domainPanel.refresh(u);
        if (analyticsPanel != null) analyticsPanel.refresh(u);
        if (historyPanel != null)   historyPanel.refresh(u);
        if (profilePanel != null)   profilePanel.refresh(u);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private void buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 20));
        brandPanel.setOpaque(false);
        JLabel logoIcon = new JLabel(IconFactory.getIcon("trophy", 20, Theme.ACCENT));
        JLabel logo = new JLabel("PlacementPrep");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(Color.WHITE);
        brandPanel.add(logoIcon);
        brandPanel.add(logo);
        sidebar.add(brandPanel);

        sidebar.add(Box.createVerticalStrut(8));

        // Navigation items
        addSidebarBtn(sidebar, "home",      "Home",      HOME,      true);
        addSidebarBtn(sidebar, "domains",   "Domains",   DOMAINS,   false);
        addSidebarBtn(sidebar, "analytics", "Analytics", ANALYTICS, false);
        addSidebarBtn(sidebar, "history",   "History",   HISTORY,   false);
        addSidebarBtn(sidebar, "profile",   "Profile",   PROFILE,   false);

        sidebar.add(Box.createVerticalGlue());

        // User card at bottom
        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setOpaque(false);
        userCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        userNameLabel.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userLevelLabel.setFont(Theme.SMALL_FONT);
        userLevelLabel.setForeground(Theme.DARK_TEXT_SUB);
        userLevelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        xpBar.setMaximum(100); xpBar.setValue(0);
        xpBar.setStringPainted(false);
        xpBar.setForeground(Theme.ACCENT);
        xpBar.setBackground(new Color(30, 41, 59));
        xpBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        xpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));

        // Logout
        ModernButton logoutBtn = new ModernButton("Logout");
        logoutBtn.setIcon(IconFactory.getIcon("logout", 14, Color.WHITE));
        logoutBtn.setIconTextGap(8);
        logoutBtn.setColors(new Color(30, 41, 59), new Color(51, 65, 85));
        logoutBtn.setFont(Theme.SMALL_FONT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> frame.showCard(MainFrame.LOGIN));

        userCard.add(userNameLabel);
        userCard.add(Box.createVerticalStrut(4));
        userCard.add(userLevelLabel);
        userCard.add(Box.createVerticalStrut(8));
        userCard.add(xpBar);
        userCard.add(Box.createVerticalStrut(12));
        userCard.add(logoutBtn);

        sidebar.add(userCard);

        add(sidebar, BorderLayout.WEST);
    }

    private void addSidebarBtn(JPanel sidebar, String iconKey, String text, String card, boolean selected) {
        SidebarBtn btn = new SidebarBtn(iconKey, text, selected);
        btn.addActionListener(e -> switchTab(card, btn));
        sidebarBtns.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void switchTab(String card, SidebarBtn active) {
        for (SidebarBtn b : sidebarBtns) b.setSelected(false);
        active.setSelected(true);
        // Lazy-init sub-panels
        ensurePanel(card);
        contentLayout.show(contentArea, card);
        refreshActivePanel(card);
    }

    private void ensurePanel(String card) {
        switch (card) {
            case HOME      -> { if (homePanel == null)      { homePanel = new HomeSubPanel(frame, user); contentArea.add(homePanel, HOME); } }
            case DOMAINS   -> { if (domainPanel == null)    { domainPanel = new DomainSubPanel(frame, user); contentArea.add(domainPanel, DOMAINS); } }
            case ANALYTICS -> { if (analyticsPanel == null) { analyticsPanel = new AnalyticsSubPanel(user); contentArea.add(analyticsPanel, ANALYTICS); } }
            case HISTORY   -> { if (historyPanel == null)   { historyPanel = new HistorySubPanel(user); contentArea.add(historyPanel, HISTORY); } }
            case PROFILE   -> { if (profilePanel == null)   { profilePanel = new ProfileSubPanel(frame, user); contentArea.add(profilePanel, PROFILE); } }
        }
    }

    private void refreshActivePanel(String card) {
        if (user == null) return;
        switch (card) {
            case HOME      -> { if (homePanel != null)      homePanel.refresh(user); }
            case DOMAINS   -> { if (domainPanel != null)    domainPanel.refresh(user); }
            case ANALYTICS -> { if (analyticsPanel != null) analyticsPanel.refresh(user); }
            case HISTORY   -> { if (historyPanel != null)   historyPanel.refresh(user); }
            case PROFILE   -> { if (profilePanel != null)   profilePanel.refresh(user); }
        }
    }

    // ── Content area ──────────────────────────────────────────────────────────

    private void buildContentArea() {
        contentArea.setBackground(Theme.DARK_BG);
        // Home is built eagerly
        homePanel = new HomeSubPanel(frame, user);
        contentArea.add(homePanel, HOME);
        add(contentArea, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sidebar button component
    // ─────────────────────────────────────────────────────────────────────────
    private static class SidebarBtn extends JButton {
        private boolean selected;
        private final Icon normalIcon;
        private final Icon activeIcon;

        public SidebarBtn(String iconKey, String text, boolean selected) {
            super(text);
            this.selected = selected;
            this.normalIcon = IconFactory.getIcon(iconKey, 16, Theme.DARK_TEXT_SUB);
            this.activeIcon = IconFactory.getIcon(iconKey, 16, Color.WHITE);
            
            setIcon(selected ? activeIcon : normalIcon);
            setIconTextGap(12);
            setFont(Theme.BODY_FONT);
            setForeground(selected ? Color.WHITE : Theme.DARK_TEXT_SUB);
            setBackground(selected ? new Color(99, 102, 241, 60) : new Color(0, 0, 0, 0));
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!SidebarBtn.this.selected) {
                        setForeground(Color.WHITE);
                        setIcon(activeIcon);
                    }
                }
                public void mouseExited(MouseEvent e)  {
                    if (!SidebarBtn.this.selected) {
                        setForeground(Theme.DARK_TEXT_SUB);
                        setIcon(normalIcon);
                    }
                }
            });
        }
        public void setSelected(boolean sel) {
            this.selected = sel;
            setForeground(sel ? Color.WHITE : Theme.DARK_TEXT_SUB);
            setIcon(sel ? activeIcon : normalIcon);
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(99, 102, 241, 55));
                g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
