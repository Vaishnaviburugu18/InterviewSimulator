package ui;

import model.Question;
import model.User;
import service.AdminService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Split admin panel for question management.
 * Left: Form for adding, updating, and deleting questions.
 * Right: Search, Filter, statistics cards, and JTable grid to select questions.
 */
public class AdminPanel extends JPanel {

    private final MainFrame frame;
    private final AdminService adminService = new AdminService();

    // Left Form Fields
    private final JTextField idField = new JTextField();
    private final JComboBox<String> topicCombo = new JComboBox<>(service.QuizService.ALL_DOMAINS);
    private final JComboBox<String> diffCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced"});
    private final JTextArea questionArea = new JTextArea(4, 30);
    private final JTextField opt1Field = new JTextField();
    private final JTextField opt2Field = new JTextField();
    private final JTextField opt3Field = new JTextField();
    private final JTextField opt4Field = new JTextField();
    private final JTextField correctField = new JTextField();
    private final JTextArea explanationArea = new JTextArea(3, 30);
    private final JTextField topicNameField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    // Right Search & Grid Fields
    private final JTextField searchField = new JTextField(15);
    private final JComboBox<String> filterTopicCombo;
    private final JComboBox<String> filterDiffCombo;
    private final JTable questionsTable;
    private final DefaultTableModel tableModel;
    private final JLabel totalStatsLabel = new JLabel("Total Questions: 0");

    private List<Question> currentQuestions = new ArrayList<>();

    public AdminPanel(MainFrame frame, User user) {
        this.frame = frame;

        // Populate filter combo options
        List<String> topicFilters = new ArrayList<>();
        topicFilters.add("All Domains");
        topicFilters.addAll(List.of(service.QuizService.ALL_DOMAINS));
        filterTopicCombo = new JComboBox<>(topicFilters.toArray(new String[0]));

        filterDiffCombo = new JComboBox<>(new String[]{"All Difficulties", "Beginner", "Intermediate", "Advanced"});

        // Table setup
        tableModel = new DefaultTableModel(new Object[]{"ID", "Domain", "Question Summary", "Difficulty"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        questionsTable = new JTable(tableModel);

        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
        loadTableData();
    }

    private void build() {
        // ─── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 28, 12, 28));
        
        JLabel title = new JLabel("Admin – Question Manager");
        title.setIcon(IconFactory.getIcon("admin", 22, Theme.ACCENT));
        title.setIconTextGap(8);
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.DARK_TEXT_MAIN);
        
        ModernButton backBtn = new ModernButton("Dashboard");
        backBtn.setIcon(IconFactory.getIcon("arrow-left", 14, Color.WHITE));
        backBtn.setColors(new Color(51, 65, 85), new Color(71, 85, 105));
        backBtn.setFont(Theme.SMALL_FONT);
        backBtn.addActionListener(e -> frame.showCard(MainFrame.DASHBOARD));
        
        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ─── Left Panel: Input Form ──────────────────────────────────────────
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Theme.DARK_BG);
        leftPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int row = 0;
        row = addFormRow(leftPanel, g, row, "Question ID (Update/Delete):", idField);
        
        topicCombo.setFont(Theme.BODY_FONT);
        topicCombo.setBackground(Theme.DARK_CARD);
        topicCombo.setForeground(Theme.DARK_TEXT_MAIN);
        row = addFormRow(leftPanel, g, row, "Domain / Topic:", topicCombo);

        diffCombo.setFont(Theme.BODY_FONT);
        diffCombo.setBackground(Theme.DARK_CARD);
        diffCombo.setForeground(Theme.DARK_TEXT_MAIN);
        row = addFormRow(leftPanel, g, row, "Difficulty:", diffCombo);

        row = addFormRow(leftPanel, g, row, "Sub-Topic Name:", topicNameField);

        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        styleTextArea(questionArea);
        g.gridx = 0; g.gridy = row; leftPanel.add(formLabel("Question Text:"), g);
        g.gridx = 1; leftPanel.add(new JScrollPane(questionArea), g);
        row++;

        row = addFormRow(leftPanel, g, row, "Option 1:", opt1Field);
        row = addFormRow(leftPanel, g, row, "Option 2:", opt2Field);
        row = addFormRow(leftPanel, g, row, "Option 3:", opt3Field);
        row = addFormRow(leftPanel, g, row, "Option 4:", opt4Field);
        row = addFormRow(leftPanel, g, row, "Correct Option:", correctField);

        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        styleTextArea(explanationArea);
        g.gridx = 0; g.gridy = row; leftPanel.add(formLabel("Explanation:"), g);
        g.gridx = 1; leftPanel.add(new JScrollPane(explanationArea), g);
        row++;

        // Status Label
        statusLabel.setFont(Theme.SMALL_FONT);
        statusLabel.setForeground(Theme.SUCCESS);
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        leftPanel.add(statusLabel, g);
        g.gridwidth = 1;
        row++;

        // Action Buttons Row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        
        ModernButton addBtn = new ModernButton("Add");
        addBtn.setIcon(IconFactory.getIcon("check", 14, Color.WHITE));
        ModernButton updateBtn = new ModernButton("Update");
        updateBtn.setIcon(IconFactory.getIcon("retry", 14, Color.WHITE));
        ModernButton deleteBtn = new ModernButton("Delete");
        deleteBtn.setIcon(IconFactory.getIcon("close", 14, Color.WHITE));
        ModernButton clearBtn = new ModernButton("Clear");
        clearBtn.setIcon(IconFactory.getIcon("refresh", 14, Color.WHITE));

        deleteBtn.setColors(Theme.DANGER, Theme.DANGER.darker());
        updateBtn.setColors(Theme.WARNING.darker(), Theme.WARNING.darker().darker());
        clearBtn.setColors(new Color(51, 65, 85), new Color(71, 85, 105));

        for (ModernButton b : new ModernButton[]{addBtn, updateBtn, deleteBtn, clearBtn}) {
            b.setPreferredSize(new Dimension(105, 34));
            btnRow.add(b);
        }

        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        leftPanel.add(btnRow, g);

        JScrollPane leftScroll = new JScrollPane(leftPanel);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(Theme.DARK_BG);

        // ─── Right Panel: Grid View + Search + Filter ────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
        rightPanel.setBackground(Theme.DARK_BG);
        rightPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Filters card
        RoundedPanel filterCard = new RoundedPanel(12, Theme.DARK_CARD);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        styleSearchField(searchField);
        filterTopicCombo.setFont(Theme.SMALL_FONT);
        filterDiffCombo.setFont(Theme.SMALL_FONT);

        JLabel searchIconLbl = new JLabel();
        searchIconLbl.setIcon(IconFactory.getIcon("search", 16, Theme.DARK_TEXT_SUB));
        filterCard.add(searchIconLbl);
        filterCard.add(searchField);
        filterCard.add(filterTopicCombo);
        filterCard.add(filterDiffCombo);

        ModernButton searchBtn = new ModernButton("Filter");
        searchBtn.setIcon(IconFactory.getIcon("search", 12, Color.WHITE));
        searchBtn.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        searchBtn.setPreferredSize(new Dimension(90, 28));
        searchBtn.addActionListener(e -> doFilterSearch());
        filterCard.add(searchBtn);

        // Stats bar
        totalStatsLabel.setFont(Theme.BODY_FONT.deriveFont(Font.BOLD));
        totalStatsLabel.setForeground(Theme.ACCENT);
        totalStatsLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel rightHeaderPanel = new JPanel(new BorderLayout());
        rightHeaderPanel.setOpaque(false);
        rightHeaderPanel.add(filterCard, BorderLayout.NORTH);
        rightHeaderPanel.add(totalStatsLabel, BorderLayout.SOUTH);
        rightPanel.add(rightHeaderPanel, BorderLayout.NORTH);

        // Scrollable JTable
        styleTable(questionsTable);
        JScrollPane tableScroll = new JScrollPane(questionsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true));
        tableScroll.getViewport().setBackground(Theme.DARK_CARD);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        // Split Pane Container
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setDividerLocation(520);
        splitPane.setDividerSize(6);
        splitPane.setBackground(Theme.DARK_BG);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        // Form Fields Styling
        for (JTextField f : new JTextField[]{idField, opt1Field, opt2Field, opt3Field, opt4Field, correctField, topicNameField}) {
            styleField(f);
        }

        // Actions
        addBtn.addActionListener(e -> doAdd());
        updateBtn.addActionListener(e -> doUpdate());
        deleteBtn.addActionListener(e -> doDelete());
        clearBtn.addActionListener(e -> clearForm());

        // Table Selection Binding to Populate Form
        questionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && questionsTable.getSelectedRow() != -1) {
                int rowIdx = questionsTable.getSelectedRow();
                populateFormFromRow(rowIdx);
            }
        });
    }

    private int addFormRow(JPanel form, GridBagConstraints g, int row, String lbl, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.3; form.add(formLabel(lbl), g);
        g.gridx = 1; g.gridy = row; g.weightx = 0.7; form.add(comp, g);
        return row + 1;
    }

    private void loadTableData() {
        try {
            currentQuestions = adminService.getAllQuestions();
            updateTableRows();
            updateStats();
        } catch (SQLException ex) {
            showError("Load failed: " + ex.getMessage());
        }
    }

    private void updateTableRows() {
        tableModel.setRowCount(0);
        for (Question q : currentQuestions) {
            String qSummary = q.getQuestion().length() > 65 ? q.getQuestion().substring(0, 62) + "..." : q.getQuestion();
            tableModel.addRow(new Object[]{
                q.getId(),
                q.getTopic(),
                qSummary,
                q.getDifficulty()
            });
        }
    }

    private void updateStats() {
        try {
            Map<String, Integer> stats = adminService.getQuestionStats();
            int total = stats.getOrDefault("Total Questions", 0);
            totalStatsLabel.setText("Total Database Questions: " + total);
        } catch (Exception ignored) {}
    }

    private void doFilterSearch() {
        String query = searchField.getText().trim();
        String topic = (String) filterTopicCombo.getSelectedItem();
        String diff = (String) filterDiffCombo.getSelectedItem();

        try {
            currentQuestions = adminService.searchQuestions(query, topic, diff);
            updateTableRows();
        } catch (SQLException ex) {
            showError("Filter failed: " + ex.getMessage());
        }
    }

    private void populateFormFromRow(int rowIdx) {
        if (rowIdx >= 0 && rowIdx < currentQuestions.size()) {
            Question q = currentQuestions.get(rowIdx);
            idField.setText(String.valueOf(q.getId()));
            topicCombo.setSelectedItem(q.getTopic());
            diffCombo.setSelectedItem(q.getDifficulty());
            topicNameField.setText(q.getTopicName());
            questionArea.setText(q.getQuestion());
            opt1Field.setText(q.getOption(0));
            opt2Field.setText(q.getOption(1));
            opt3Field.setText(q.getOption(2));
            opt4Field.setText(q.getOption(3));
            correctField.setText(q.getCorrectAnswer());
            explanationArea.setText(q.getExplanation());
            statusLabel.setText("Selected Question ID: " + q.getId());
        }
    }

    private void clearForm() {
        idField.setText("");
        topicNameField.setText("");
        questionArea.setText("");
        opt1Field.setText("");
        opt2Field.setText("");
        opt3Field.setText("");
        opt4Field.setText("");
        correctField.setText("");
        explanationArea.setText("");
        statusLabel.setText(" ");
        questionsTable.clearSelection();
    }

    private void doAdd() {
        try {
            adminService.addQuestion(buildQuestion(0));
            statusLabel.setForeground(Theme.SUCCESS);
            statusLabel.setText("Question added successfully.");
            clearForm();
            loadTableData();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void doUpdate() {
        try {
            int id = parseId();
            adminService.updateQuestion(buildQuestion(id));
            statusLabel.setForeground(Theme.SUCCESS);
            statusLabel.setText("Question updated (ID " + id + ").");
            clearForm();
            loadTableData();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void doDelete() {
        try {
            int id = parseId();
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete question ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                adminService.deleteQuestion(id);
                statusLabel.setForeground(Theme.SUCCESS);
                statusLabel.setText("Question deleted (ID " + id + ").");
                clearForm();
                loadTableData();
            }
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private int parseId() throws ValidationException {
        String raw = idField.getText().trim();
        if (raw.isEmpty()) throw new ValidationException("Question ID is required.");
        try { return Integer.parseInt(raw); }
        catch (NumberFormatException e) { throw new ValidationException("ID must be an integer."); }
    }

    private Question buildQuestion(int id) {
        return new Question(id,
            (String) topicCombo.getSelectedItem(),
            questionArea.getText(),
            opt1Field.getText(), opt2Field.getText(),
            opt3Field.getText(), opt4Field.getText(),
            correctField.getText(),
            (String) diffCombo.getSelectedItem(),
            explanationArea.getText(),
            topicNameField.getText().isBlank() ? (String) topicCombo.getSelectedItem() : topicNameField.getText());
    }

    private void showError(String msg) {
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setText("Error: " + msg);
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        l.setForeground(Theme.DARK_TEXT_SUB);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(Theme.BODY_FONT);
        f.setBackground(Theme.DARK_CARD);
        f.setForeground(Theme.DARK_TEXT_MAIN);
        f.setCaretColor(Theme.DARK_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
    }

    private void styleTextArea(JTextArea a) {
        a.setFont(Theme.BODY_FONT);
        a.setBackground(Theme.DARK_CARD);
        a.setForeground(Theme.DARK_TEXT_MAIN);
        a.setCaretColor(Theme.DARK_TEXT_MAIN);
        a.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
    }

    private void styleSearchField(JTextField f) {
        f.setFont(Theme.SMALL_FONT);
        f.setBackground(Theme.DARK_BG);
        f.setForeground(Theme.DARK_TEXT_MAIN);
        f.setCaretColor(Theme.DARK_TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleTable(JTable table) {
        table.setBackground(Theme.DARK_CARD);
        table.setForeground(Theme.DARK_TEXT_MAIN);
        table.setFont(Theme.SMALL_FONT);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(99, 102, 241, 70));
        table.setSelectionForeground(Color.WHITE);
        table.setFocusable(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Topic
        table.getColumnModel().getColumn(2).setPreferredWidth(280); // Summary
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Difficulty
    }
}
