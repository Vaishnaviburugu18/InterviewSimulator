package ui;

import model.User;
import service.AuthenticationException;
import service.LoginService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame {
    private final LoginService loginService = new LoginService();
    private final JTabbedPane tabs = new JTabbedPane();

    public LoginPage() {
        setTitle("Interview Simulator System - Login");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));

        JLabel title = new JLabel("Interview Simulator System", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        root.add(title, BorderLayout.NORTH);

        tabs.setFont(new Font("Arial", Font.PLAIN, 14));

        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username or Email");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField userField = new JTextField();
        userField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel tip = new JLabel("Tip: seed users are inserted in the SQL script.");
        tip.setForeground(new Color(160, 190, 255));
        tip.setFont(new Font("Arial", Font.PLAIN, 12));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(userLabel, gbc);

        gbc.gridy = 1;
        panel.add(userField, gbc);

        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        gbc.gridy = 3;
        panel.add(passField, gbc);

        gbc.gridy = 4;
        panel.add(loginBtn, gbc);

        gbc.gridy = 5;
        panel.add(tip, gbc);

        loginBtn.addActionListener(e -> {
            String usernameOrEmail = userField.getText();
            String password = new String(passField.getPassword());
            try {
                User user = loginService.login(usernameOrEmail, password);
                SwingUtilities.invokeLater(() -> {
                    new Dashboard(user);
                    dispose();
                });
            } catch (AuthenticationException | ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Service Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel note = new JLabel("Password is stored as SHA-256 hash (seed + created users).");
        note.setForeground(new Color(160, 190, 255));
        note.setFont(new Font("Arial", Font.PLAIN, 12));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 2;
        panel.add(emailLabel, gbc);

        gbc.gridy = 3;
        panel.add(emailField, gbc);

        gbc.gridy = 4;
        panel.add(passLabel, gbc);

        gbc.gridy = 5;
        panel.add(passField, gbc);

        gbc.gridy = 6;
        panel.add(registerBtn, gbc);

        gbc.gridy = 7;
        panel.add(note, gbc);

        registerBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passField.getPassword());

            try {
                User user = loginService.registerUser(username, email, password);
                JOptionPane.showMessageDialog(this, "Registration successful. Please login now.");
                // Switch back to login tab.
                tabs.setSelectedIndex(0);
                usernameField.setText("");
                emailField.setText("");
                passField.setText("");
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Service Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }
}

