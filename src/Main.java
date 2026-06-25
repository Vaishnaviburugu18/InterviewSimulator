import database.DatabaseInitializer;
import database.DatabaseSeeder;
import ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply system look-and-feel for native rendering hints
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global font anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Run auto-migrations (create/alter tables as needed)
                DatabaseInitializer.initializeDatabase();
                // 2. Seed 900 questions if not already present
                DatabaseSeeder.seedIfNeeded();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Database initialisation failed:\n" + ex.getMessage() +
                    "\n\nPlease verify that the sqlite-jdbc library is in the 'lib' folder.",
                    "DB Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            // 3. Launch single-window app
            MainFrame.get();
        });
    }
}
