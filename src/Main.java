import javax.swing.SwingUtilities;

import ui.LoginPage;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}

