package ui;

import service.LoginService;
import service.ServiceException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterPanel extends JPanel {

    private final MainFrame frame;
    private final LoginService loginService = new LoginService();

    private final JTextField userField  = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passField  = new JPasswordField();
    private final JPasswordField pass2Field = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");
    private final JLabel successLabel = new JLabel(" ");

    public RegisterPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
    }

    private void build() {
        // Left hero
        GradientPanel hero = new GradientPanel(
                new Color(5, 150, 105), new Color(16, 185, 129), false);
        hero.setLayout(new GridBagLayout());
        hero.setPreferredSize(new Dimension(520, 0));

        JPanel heroContent = new JPanel();
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setOpaque(false);
        heroContent.setBorder(new EmptyBorder(0, 40, 0, 40));

        JLabel brand = new JLabel("Join PlacementPrep Pro");
        brand.setIcon(IconFactory.getIcon("trophy", 28, Color.WHITE));
        brand.setIconTextGap(10);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 26));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html><div style='width:320px'>Start your placement journey with 900+ questions, company-specific tests, and real-time performance analytics.</div></html>");
        tagline.setFont(Theme.BODY_FONT);
        tagline.setForeground(new Color(167, 243, 208));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(brand);
        heroContent.add(Box.createVerticalStrut(20));
        heroContent.add(tagline);
        hero.add(heroContent);
        add(hero, BorderLayout.WEST);

        // Right card
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.DARK_BG);

        RoundedPanel card = new RoundedPanel(20, Theme.DARK_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 40, 35, 40));
        card.setPreferredSize(new Dimension(420, 560));

        JLabel heading = new JLabel("Create Account");
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Fill in your details to get started");
        sub.setFont(Theme.BODY_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleField(userField);
        styleField(emailField);
        styleField(passField);
        styleField(pass2Field);

        ModernButton registerBtn = new ModernButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setColors(Theme.SUCCESS, Theme.SUCCESS.darker());

        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.SMALL_FONT);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        successLabel.setForeground(Theme.SUCCESS);
        successLabel.setFont(Theme.SMALL_FONT);
        successLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hasAcct = new JLabel("Already have an account? ");
        hasAcct.setForeground(Theme.DARK_TEXT_SUB);
        hasAcct.setFont(Theme.SMALL_FONT);
        JLabel loginLink = new JLabel("Sign In");
        loginLink.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        loginLink.setForeground(Theme.ACCENT);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { frame.showCard(MainFrame.LOGIN); }
            public void mouseEntered(MouseEvent e) { loginLink.setForeground(Theme.ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { loginLink.setForeground(Theme.ACCENT); }
        });
        linkPanel.add(hasAcct); linkPanel.add(loginLink);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(labelFor("Username")); card.add(Box.createVerticalStrut(6));
        card.add(userField); card.add(Box.createVerticalStrut(12));
        card.add(labelFor("Email Address")); card.add(Box.createVerticalStrut(6));
        card.add(emailField); card.add(Box.createVerticalStrut(12));
        card.add(labelFor("Password")); card.add(Box.createVerticalStrut(6));
        card.add(passField); card.add(Box.createVerticalStrut(12));
        card.add(labelFor("Confirm Password")); card.add(Box.createVerticalStrut(6));
        card.add(pass2Field);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(successLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(linkPanel);

        right.add(card);
        add(right, BorderLayout.CENTER);

        registerBtn.addActionListener(e -> doRegister());
    }

    private void doRegister() {
        errorLabel.setText(" "); successLabel.setText(" ");
        String user  = userField.getText().trim();
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword());
        String pass2 = new String(pass2Field.getPassword());

        if (!pass.equals(pass2)) { errorLabel.setText("Passwords do not match."); return; }
        try {
            loginService.registerUser(user, email, pass);
            successLabel.setText("Account created! You can now sign in.");
            userField.setText(""); emailField.setText(""); passField.setText(""); pass2Field.setText("");
            Timer t = new Timer(1500, e -> frame.showCard(MainFrame.LOGIN));
            t.setRepeats(false); t.start();
        } catch (ServiceException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    private JLabel labelFor(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        l.setForeground(Theme.DARK_TEXT_SUB);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField field) {
        field.setFont(Theme.BODY_FONT);
        field.setBackground(new Color(15, 23, 42));
        field.setForeground(Theme.DARK_TEXT_MAIN);
        field.setCaretColor(Theme.DARK_TEXT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new javax.swing.border.EmptyBorder(10, 14, 10, 14)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
