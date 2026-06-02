package ui;

import model.Question;
import model.User;
import service.AdminService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AdminPage extends JFrame {
    private final AdminService adminService = new AdminService();
    private final User user;

    private final JTextField idField = new JTextField();
    private final JComboBox<String> topicCombo = new JComboBox<>(new String[] {"Java", "DSA", "DBMS", "OS"});
    private final JTextArea questionArea = new JTextArea();
    private final JTextField option1Field = new JTextField();
    private final JTextField option2Field = new JTextField();
    private final JTextField option3Field = new JTextField();
    private final JTextField option4Field = new JTextField();
    private final JTextField correctAnswerField = new JTextField();

    public AdminPage(User user) {
        this.user = user;
        setTitle("Admin - Questions Manager (Optional)");
        setSize(980, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Admin: Add / Update / Delete Questions", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        questionArea.setRows(4);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 120)));

        int y = 0;

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Question ID (for update/delete)"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        idField.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Topic"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        topicCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(topicCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel qLabel = new JLabel("Question");
        qLabel.setVerticalAlignment(SwingConstants.TOP);
        form.add(qLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        form.add(questionScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Option 1"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        option1Field.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(option1Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Option 2"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        option2Field.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(option2Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Option 3"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        option3Field.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(option3Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Option 4"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        option4Field.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(option4Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = y;
        form.add(new JLabel("Correct Answer (must match one option)"), gbc);
        gbc.gridx = 1;
        gbc.gridy = y++;
        correctAnswerField.setFont(new Font("Arial", Font.PLAIN, 14));
        form.add(correctAnswerField, gbc);

        root.add(form, BorderLayout.CENTER);

        JButton addBtn = new JButton("Add Question");
        JButton updateBtn = new JButton("Update Question");
        JButton deleteBtn = new JButton("Delete Question");
        JButton closeBtn = new JButton("Close");

        addBtn.setFont(new Font("Arial", Font.BOLD, 14));
        updateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        bottom.setBackground(new Color(15, 23, 42));
        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(deleteBtn);
        bottom.add(closeBtn);
        root.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                Question q = buildQuestion(0);
                adminService.addQuestion(q);
                JOptionPane.showMessageDialog(this, "Question added successfully.");
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                int id = parseId();
                Question q = buildQuestion(id);
                adminService.updateQuestion(q);
                JOptionPane.showMessageDialog(this, "Question updated successfully.");
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            try {
                int id = parseId();
                adminService.deleteQuestion(id);
                JOptionPane.showMessageDialog(this, "Question deleted successfully.");
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> {
            new Dashboard(user).setVisible(true);
            dispose();
        });

        setContentPane(root);
        setVisible(true);
    }

    private int parseId() throws ValidationException {
        String raw = idField.getText().trim();
        if (raw.isEmpty()) throw new ValidationException("Question ID is required for update/delete.");
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Question ID must be an integer.");
        }
    }

    private Question buildQuestion(int id) {
        String topic = (String) topicCombo.getSelectedItem();
        String question = questionArea.getText();
        return new Question(
                id,
                topic,
                question,
                option1Field.getText(),
                option2Field.getText(),
                option3Field.getText(),
                option4Field.getText(),
                correctAnswerField.getText()
        );
    }
}

