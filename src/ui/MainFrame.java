package ui;

import model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Single JFrame that hosts every panel via CardLayout.
 * Call MainFrame.show(NAME) to switch screens.
 */
public class MainFrame extends JFrame {

    // Card names
    public static final String LOGIN    = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String DASHBOARD= "DASHBOARD";
    public static final String QUIZ     = "QUIZ";
    public static final String RESULT   = "RESULT";
    public static final String ADMIN    = "ADMIN";

    private static MainFrame instance;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     container  = new JPanel(cardLayout);

    // Panels (created once, reused)
    private LoginPanel    loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;
    private QuizPanel      quizPanel;
    private ResultPanel    resultPanel;
    private AdminPanel     adminPanel;

    private User currentUser;

    private MainFrame() {
        setTitle("PlacementPrep Pro – Interview Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        container.setBackground(Theme.getBg());
        setContentPane(container);

        // Build static panels
        loginPanel    = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);

        container.add(loginPanel,    LOGIN);
        container.add(registerPanel, REGISTER);

        // Dashboard + Quiz + Result + Admin are created lazily after login
        cardLayout.show(container, LOGIN);
        setVisible(true);
    }

    public static MainFrame get() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    /** Navigate to a named card. */
    public void showCard(String name) {
        cardLayout.show(container, name);
    }

    /** Switch to Dashboard (builds it lazily the first time). */
    public void goToDashboard(User user) {
        this.currentUser = user;
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(this);
            container.add(dashboardPanel, DASHBOARD);
        } else {
            dashboardPanel.refresh(user);
        }
        dashboardPanel.setUser(user);
        cardLayout.show(container, DASHBOARD);
    }

    /** Start a quiz session. */
    public void startQuiz(User user, String domain, String mode, String company) {
        this.currentUser = user;
        // Remove old quiz/result panels so fresh state is used every time
        if (quizPanel != null)   container.remove(quizPanel);
        if (resultPanel != null) container.remove(resultPanel);
        quizPanel  = new QuizPanel(this, user, domain, mode, company);
        resultPanel = null;
        container.add(quizPanel, QUIZ);
        cardLayout.show(container, QUIZ);
    }

    /** Called by QuizPanel when an attempt is finished. */
    public void showResult(User user, String domain, String mode,
                           java.util.List<model.Question> questions,
                           java.util.List<String> answers) {
        if (resultPanel != null) container.remove(resultPanel);
        resultPanel = new ResultPanel(this, user, domain, mode, questions, answers);
        container.add(resultPanel, RESULT);
        cardLayout.show(container, RESULT);
    }

    public void goToAdmin(User user) {
        if (adminPanel == null) {
            adminPanel = new AdminPanel(this, user);
            container.add(adminPanel, ADMIN);
        }
        cardLayout.show(container, ADMIN);
    }

    public User getCurrentUser() { return currentUser; }

    public void reloadTheme() {
        // Repaint all children
        SwingUtilities.updateComponentTreeUI(this);
        container.revalidate();
        container.repaint();
    }
}
