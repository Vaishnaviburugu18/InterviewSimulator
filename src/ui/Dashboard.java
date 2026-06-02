package ui;

import model.User;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private final User user;
    private String selectedTopic;

    private final JButton startQuizBtn = new JButton("Start Quiz");
    private final JButton historyBtn = new JButton("View Previous Results");
    private final JButton logoutBtn = new JButton("Logout");
    private final JButton adminBtn = new JButton("Admin (Optional)");

    public Dashboard(User user) {
        this.user = user;

        setTitle("Dashboard");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));

        JLabel welcome = new JLabel("Welcome, " + user.getUsername(), SwingConstants.CENTER);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        root.add(welcome, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel topicPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        topicPanel.setOpaque(false);
        topicPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        String[] topics = {"Java", "DSA", "DBMS", "OS"};
        JButton[] topicButtons = new JButton[topics.length];

        for (int i = 0; i < topics.length; i++) {
            JButton b = new JButton(topics[i] + " Quiz");
            b.setFont(new Font("Arial", Font.BOLD, 18));
            b.setFocusPainted(false);
            b.setBackground(new Color(40, 56, 87));
            b.setForeground(Color.WHITE);

            String topic = topics[i];
            b.addActionListener(e -> {
                selectedTopic = topic;
                startQuizBtn.setEnabled(true);
                // simple visual selection
                for (JButton tb : topicButtons) {
                    if (tb == null) continue;
                    tb.setBackground(new Color(40, 56, 87));
                }
                b.setBackground(new Color(0, 120, 215));
            });

            topicButtons[i] = b;
            topicPanel.add(b);
        }

        center.add(topicPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        bottom.setOpaque(false);

        startQuizBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startQuizBtn.setEnabled(false);
        historyBtn.setFont(new Font("Arial", Font.BOLD, 14));
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        adminBtn.setFont(new Font("Arial", Font.BOLD, 14));

        bottom.add(startQuizBtn);
        bottom.add(historyBtn);
        bottom.add(adminBtn);
        bottom.add(logoutBtn);
        center.add(bottom, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);

        boolean isAdmin = "admin".equalsIgnoreCase(user.getUsername());
        adminBtn.setVisible(isAdmin);

        startQuizBtn.addActionListener(e -> {
            if (selectedTopic == null) return;
            new QuizPage(user.getUsername(), selectedTopic);
            dispose();
        });
        historyBtn.addActionListener(e -> {
            new ResultsHistoryPage(user);
            dispose();
        });
        adminBtn.addActionListener(e -> {
            if (!isAdmin) return;
            new AdminPage(user);
            dispose();
        });
        logoutBtn.addActionListener(e -> {
            new LoginPage();
            dispose();
        });

        setVisible(true);
    }
}

