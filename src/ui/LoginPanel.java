package ui;

import model.User;
import service.LoginService;
import service.AuthenticationException;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {

    private final MainFrame frame;
    private final LoginService loginService = new LoginService();

    private final JTextField userField    = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JLabel errorLabel       = new JLabel(" ");

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Theme.DARK_BG);
        build();
    }

    private void build() {
        // ── Left hero panel ─────────────────────────────────────────────────
        GradientPanel hero = new GradientPanel(
            new Color(67, 56, 202), new Color(99, 102, 241), false);
        hero.setLayout(new GridBagLayout());
        hero.setPreferredSize(new Dimension(520, 0));

        JPanel heroContent = new JPanel();
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setOpaque(false);
        heroContent.setBorder(new EmptyBorder(0, 40, 0, 40));

        JLabel brand = new JLabel("PlacementPrep Pro");
        brand.setIcon(IconFactory.getIcon("trophy", 28, Color.WHITE));
        brand.setIconTextGap(10);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html><div style='width:320px'>Your ultimate placement preparation platform with 30 domains, company-specific tests, and AI-powered recommendations.</div></html>");
        tagline.setFont(Theme.BODY_FONT);
        tagline.setForeground(new Color(199, 210, 254));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(brand);
        heroContent.add(Box.createVerticalStrut(20));
        heroContent.add(tagline);
        heroContent.add(Box.createVerticalStrut(40));

        // Feature pills
        String[] features = {"30 Domains", "900+ Questions",
                             "Company Tests", "Analytics & XP"};
        for (String f : features) {
            JLabel pill = new JLabel(f);
            pill.setIcon(IconFactory.getIcon("check", 14, new Color(224, 231, 255)));
            pill.setIconTextGap(8);
            pill.setFont(Theme.BODY_FONT);
            pill.setForeground(new Color(224, 231, 255));
            pill.setAlignmentX(Component.LEFT_ALIGNMENT);
            heroContent.add(pill);
            heroContent.add(Box.createVerticalStrut(10));
        }

        hero.add(heroContent);
        add(hero, BorderLayout.WEST);

        // ── Right login card ─────────────────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.DARK_BG);

        RoundedPanel card = new RoundedPanel(20, Theme.DARK_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(420, 480));

        JLabel heading = new JLabel("Welcome Back");
        heading.setFont(Theme.TITLE_FONT);
        heading.setForeground(Theme.DARK_TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue your journey");
        sub.setFont(Theme.BODY_FONT);
        sub.setForeground(Theme.DARK_TEXT_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleField(userField, "Username or Email");
        styleField(passField, "Password");

        ModernButton loginBtn = new ModernButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setFont(Theme.SUBTITLE_FONT.deriveFont(Font.BOLD, 15f));

        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.SMALL_FONT);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcct = new JLabel("Don't have an account? ");
        noAcct.setForeground(Theme.DARK_TEXT_SUB);
        noAcct.setFont(Theme.SMALL_FONT);
        JLabel regLink = new JLabel("Register");
        regLink.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        regLink.setForeground(Theme.ACCENT);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { frame.showCard(MainFrame.REGISTER); }
            public void mouseEntered(MouseEvent e) { regLink.setForeground(Theme.ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { regLink.setForeground(Theme.ACCENT); }
        });
        linkPanel.add(noAcct); linkPanel.add(regLink);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(30));
        card.add(labelFor("Username or Email"));
        card.add(Box.createVerticalStrut(6));
        card.add(userField);
        card.add(Box.createVerticalStrut(16));
        card.add(labelFor("Password"));
        card.add(Box.createVerticalStrut(6));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(linkPanel);

        right.add(card);
        add(right, BorderLayout.CENTER);

        // ── Actions ──────────────────────────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        userField.addActionListener(e -> passField.requestFocus());
    }

    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        errorLabel.setText(" ");
        try {
            User u = loginService.login(user, pass);
            frame.goToDashboard(u);
        } catch (AuthenticationException | ValidationException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            errorLabel.setText("Database connection error. Try restarting.");
        }
    }

    private JLabel labelFor(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL_FONT.deriveFont(Font.BOLD));
        l.setForeground(Theme.DARK_TEXT_SUB);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(Theme.BODY_FONT);
        field.setBackground(new Color(15, 23, 42));
        field.setForeground(Theme.DARK_TEXT_MAIN);
        field.setCaretColor(Theme.DARK_TEXT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DARK_BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
