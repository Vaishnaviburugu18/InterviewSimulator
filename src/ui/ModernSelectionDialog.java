package ui;

import model.User;
import service.QuizService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A modern, dark-themed custom selection dialog that replaces the standard Swing JOptionPane dialogs.
 * It provides dynamic search, hover effects, rounded cards, and category grouping.
 */
public class ModernSelectionDialog extends JDialog {

    private String selectedValue = null;
    private final JTextField searchField = new JTextField();
    private final JPanel cardsPanel = new JPanel();
    private final List<CardItem> cardItems = new ArrayList<>();
    private final ModernButton actionBtn = new ModernButton("Start Quiz");

    // Categories for the domains
    private static final String CAT_CORE_CS = "Core CS & Programming";
    private static final String CAT_WEB_ENG = "Web & Software Engineering";
    private static final String CAT_AI_DATA = "Data Science & AI";
    private static final String CAT_APTITUDE = "Placement Aptitude";
    private static final String CAT_INTERVIEW = "Interview & HR Prep";

    private ModernSelectionDialog(Frame owner, String title, String prompt, String[] items, String type) {
        super(owner, title, true);
        setSize(850, 600);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(owner);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(Theme.DARK_BORDER, 2, true));

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Theme.DARK_BG);
        contentPane.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(contentPane);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.TITLE_FONT);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.WEST);

        // Close Button
        JButton closeBtn = new JButton(IconFactory.getIcon("close", 18, Theme.DARK_TEXT_SUB));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        headerPanel.add(closeBtn, BorderLayout.EAST);

        JLabel promptLbl = new JLabel(prompt);
        promptLbl.setFont(Theme.BODY_FONT);
        promptLbl.setForeground(Theme.DARK_TEXT_SUB);
        headerPanel.add(promptLbl, BorderLayout.SOUTH);

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Selection Grid/List
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Search Bar (Only for domains/companies if they are large lists)
        if ("domain".equalsIgnoreCase(type)) {
            JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0));
            searchBarPanel.setOpaque(false);
            searchBarPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

            JLabel searchIcon = new JLabel(IconFactory.getIcon("search", 18, Theme.DARK_TEXT_SUB));
            searchBarPanel.add(searchIcon, BorderLayout.WEST);

            styleField(searchField);
            searchField.setPreferredSize(new Dimension(0, 38));
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filterCards(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filterCards(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filterCards(); }
            });
            searchBarPanel.add(searchField, BorderLayout.CENTER);
            centerContainer.add(searchBarPanel, BorderLayout.NORTH);
        }

        // Cards Scroll Pane
        cardsPanel.setBackground(Theme.DARK_BG);
        cardsPanel.setLayout(new GridBagLayout());
        
        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 8));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerContainer.add(scroll, BorderLayout.CENTER);
        contentPane.add(centerContainer, BorderLayout.CENTER);

        // Bottom Button Row
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        bottomBar.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel");
        cancelBtn.setColors(new Color(51, 65, 85), new Color(71, 85, 105));
        cancelBtn.setPreferredSize(new Dimension(100, 36));
        cancelBtn.addActionListener(e -> dispose());
        bottomBar.add(cancelBtn);

        actionBtn.setColors(Theme.ACCENT, Theme.ACCENT_HOVER);
        actionBtn.setPreferredSize(new Dimension(120, 36));
        actionBtn.setEnabled(false);
        actionBtn.addActionListener(e -> {
            if (selectedValue != null) {
                dispose();
            }
        });
        bottomBar.add(actionBtn);

        contentPane.add(bottomBar, BorderLayout.SOUTH);

        // Populate Cards
        populateItems(items, type);
    }

    private void populateItems(String[] items, String type) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);

        if ("domain".equalsIgnoreCase(type)) {
            // Group domains by category
            addCategoryGroup(CAT_CORE_CS, getDomainsForCat(CAT_CORE_CS), gbc);
            addCategoryGroup(CAT_WEB_ENG, getDomainsForCat(CAT_WEB_ENG), gbc);
            addCategoryGroup(CAT_AI_DATA, getDomainsForCat(CAT_AI_DATA), gbc);
            addCategoryGroup(CAT_APTITUDE, getDomainsForCat(CAT_APTITUDE), gbc);
            addCategoryGroup(CAT_INTERVIEW, getDomainsForCat(CAT_INTERVIEW), gbc);
        } else {
            // Flattened layout for mode or company
            JPanel grid = new JPanel(new GridLayout(0, "mode".equalsIgnoreCase(type) ? 3 : 4, 12, 12));
            grid.setOpaque(false);
            for (String item : items) {
                CardItem card = new CardItem(item, getIconNameForItem(item, type), getDescriptionForItem(item, type));
                cardItems.add(card);
                grid.add(card);
            }
            cardsPanel.add(grid, gbc);
        }
        
        // Push everything to top
        gbc.weighty = 1.0;
        cardsPanel.add(Box.createGlue(), gbc);
    }

    private void addCategoryGroup(String title, List<String> domains, GridBagConstraints gbc) {
        if (domains.isEmpty()) return;

        JPanel catPanel = new JPanel(new BorderLayout(0, 10));
        catPanel.setOpaque(false);
        catPanel.setName(title); // Store category title for search matching

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.SUBTITLE_FONT.deriveFont(Font.BOLD, 15f));
        titleLbl.setForeground(Theme.ACCENT);
        titleLbl.setBorder(new EmptyBorder(8, 4, 4, 4));
        catPanel.add(titleLbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setOpaque(false);
        for (String d : domains) {
            CardItem card = new CardItem(d, d, null);
            cardItems.add(card);
            grid.add(card);
        }
        catPanel.add(grid, BorderLayout.CENTER);

        cardsPanel.add(catPanel, gbc);
        gbc.gridy++;
    }

    private void filterCards() {
        String query = searchField.getText().trim().toLowerCase();
        
        // Loop through all category panels
        for (Component comp : cardsPanel.getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null) {
                JPanel catPanel = (JPanel) comp;
                JPanel grid = null;
                for (Component child : catPanel.getComponents()) {
                    if (child instanceof JPanel) {
                        grid = (JPanel) child;
                        break;
                    }
                }
                
                if (grid != null) {
                    int visibleCount = 0;
                    for (Component cardComp : grid.getComponents()) {
                        if (cardComp instanceof CardItem) {
                            CardItem card = (CardItem) cardComp;
                            boolean matches = card.getItemTitle().toLowerCase().contains(query);
                            card.setVisible(matches);
                            if (matches) visibleCount++;
                        }
                    }
                    catPanel.setVisible(visibleCount > 0);
                }
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void selectCard(CardItem selectedCard) {
        for (CardItem c : cardItems) {
            c.setSelected(c == selectedCard);
        }
        selectedValue = selectedCard.getItemTitle();
        actionBtn.setEnabled(true);
    }

    // --- Static Launch Helpers ---

    public static String showDomainSelection(Frame owner, String title, String prompt) {
        ModernSelectionDialog dialog = new ModernSelectionDialog(owner, title, prompt, QuizService.ALL_DOMAINS, "domain");
        dialog.setVisible(true);
        return dialog.selectedValue;
    }

    public static String showModeSelection(Frame owner, String title, String prompt) {
        String[] modes = {"Practice", "Timed", "Mock"};
        ModernSelectionDialog dialog = new ModernSelectionDialog(owner, title, prompt, modes, "mode");
        dialog.setVisible(true);
        return dialog.selectedValue;
    }

    public static String showCompanySelection(Frame owner, String title, String prompt) {
        String[] companies = {"TCS", "Infosys", "Wipro", "Accenture", "Cognizant", "Deloitte", "UBS", "AutoRABIT"};
        ModernSelectionDialog dialog = new ModernSelectionDialog(owner, title, prompt, companies, "company");
        dialog.setVisible(true);
        return dialog.selectedValue;
    }

    // --- Domain Categories Helpers ---

    private List<String> getDomainsForCat(String cat) {
        List<String> list = new ArrayList<>();
        for (String d : QuizService.ALL_DOMAINS) {
            if (cat.equals(getCategoryForDomain(d))) {
                list.add(d);
            }
        }
        return list;
    }

    private String getCategoryForDomain(String domain) {
        return switch (domain) {
            case "Data Structures and Algorithms", "Operating Systems",
                 "Database Management Systems", "Computer Networks",
                 "Object Oriented Programming", "Java", "Python",
                 "C Programming", "C++", "System Design" -> CAT_CORE_CS;

            case "Web Development", "HTML", "CSS", "JavaScript", "React",
                 "SQL", "Software Engineering", "DevOps", "Cloud Computing" -> CAT_WEB_ENG;

            case "Artificial Intelligence", "Machine Learning", "Data Science" -> CAT_AI_DATA;

            case "Aptitude", "Logical Reasoning", "Quantitative Aptitude", "Verbal Ability" -> CAT_APTITUDE;

            case "HR Interview", "Behavioral Interview", "Group Discussion Preparation" -> CAT_INTERVIEW;

            default -> CAT_CORE_CS;
        };
    }

    private String getIconNameForItem(String item, String type) {
        if ("mode".equalsIgnoreCase(type)) {
            return switch (item) {
                case "Practice" -> "book";
                case "Timed" -> "timer";
                case "Mock" -> "trophy";
                default -> "book";
            };
        } else if ("company".equalsIgnoreCase(type)) {
            return "user"; // Standard profile icon for companies
        }
        return item;
    }

    private String getDescriptionForItem(String item, String type) {
        if ("mode".equalsIgnoreCase(type)) {
            return switch (item) {
                case "Practice" -> "No timer pressures. Get instant correct option check & explanations.";
                case "Timed" -> "30-second countdown per question. Simulates real online rounds.";
                case "Mock" -> "Standard 10-question test (Beginner to Advanced) with single timer.";
                default -> "";
            };
        } else if ("company".equalsIgnoreCase(type)) {
            return "Tuned syllabus containing typical online test patterns for " + item + ".";
        }
        return null;
    }

    // --- Inner Components ---

    private class CardItem extends RoundedPanel {
        private final String itemTitle;
        private final JLabel iconLabel;
        private final JLabel titleLabel;
        private JTextArea descArea;
        private boolean isSelected = false;
        private final Color borderAccent = Theme.ACCENT;

        public CardItem(String itemTitle, String iconKey, String description) {
            super(12, Theme.DARK_CARD);
            this.itemTitle = itemTitle;
            setLayout(new BorderLayout(12, 10));
            setBorder(new EmptyBorder(14, 14, 14, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Icon Column
            iconLabel = new JLabel(IconFactory.getIcon(getIconName(iconKey), 28, Theme.DARK_TEXT_SUB));
            add(iconLabel, BorderLayout.WEST);

            // Text Panel
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            titleLabel = new JLabel(itemTitle);
            titleLabel.setFont(Theme.CARD_TITLE_FONT.deriveFont(13f));
            titleLabel.setForeground(Theme.DARK_TEXT_MAIN);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPanel.add(titleLabel);

            if (description != null) {
                textPanel.add(Box.createVerticalStrut(4));
                descArea = new JTextArea(description);
                descArea.setFont(Theme.SMALL_FONT);
                descArea.setForeground(Theme.DARK_TEXT_SUB);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                descArea.setOpaque(false);
                descArea.setEditable(false);
                descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                textPanel.add(descArea);
            }

            add(textPanel, BorderLayout.CENTER);

            // Click & Hover listeners
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        setBackground(new Color(51, 65, 85));
                        setBorderOverride(Theme.DARK_BORDER, 1);
                        repaint();
                    }
                }
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        setBackground(Theme.DARK_CARD);
                        setBorderOverride(null, 0);
                        repaint();
                    }
                }
                public void mouseClicked(MouseEvent e) {
                    selectCard(CardItem.this);
                }
            });
        }

        public String getItemTitle() {
            return itemTitle;
        }

        public void setSelected(boolean sel) {
            this.isSelected = sel;
            setBackground(sel ? new Color(99, 102, 241, 40) : Theme.DARK_CARD);
            setBorderOverride(sel ? borderAccent : null, sel ? 1 : 0);
            titleLabel.setForeground(sel ? Color.WHITE : Theme.DARK_TEXT_MAIN);
            iconLabel.setIcon(IconFactory.getIcon(getIconName(itemTitle), 28, sel ? Theme.ACCENT : Theme.DARK_TEXT_SUB));
            repaint();
        }

        private String getIconName(String title) {
            // Maps the domain/mode/company title to matching IconFactory vector drawing names
            String lower = title.toLowerCase();
            if (lower.contains("java") && !lower.contains("javascript")) return "gear"; // gear or fallback
            if (lower.contains("python")) return "gear";
            if (lower.contains("html") || lower.contains("css") || lower.contains("web")) return "book";
            if (lower.contains("sql") || lower.contains("dbms") || lower.contains("database")) return "book";
            if (lower.contains("network")) return "settings";
            if (lower.contains("operating")) return "settings";
            if (lower.contains("devops")) return "logout"; // exits or launcher
            if (lower.contains("security")) return "lock";
            if (lower.contains("analytics") || lower.contains("data science") || lower.contains("performance")) return "analytics";
            if (lower.contains("aptitude") || lower.contains("reasoning")) return "list";
            if (lower.contains("timed")) return "timer";
            if (lower.contains("mock") || lower.contains("trophy")) return "trophy";
            if (lower.contains("practice") || lower.contains("book")) return "book";
            return "book"; // default book
        }
    }

    private void styleField(JTextField f) {
        f.setFont(Theme.BODY_FONT);
        f.setBackground(Theme.DARK_CARD);
        f.setForeground(Theme.DARK_TEXT_MAIN);
        f.setCaretColor(Theme.DARK_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
    }

    private void styleBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
