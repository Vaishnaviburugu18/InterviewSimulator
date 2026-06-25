package ui;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    public static boolean isDarkMode = true; // Dark mode by default

    // Dark Mode Colors
    public static final Color DARK_BG = new Color(15, 23, 42);       // Slate 900
    public static final Color DARK_CARD = new Color(30, 41, 59);     // Slate 800
    public static final Color DARK_TEXT_MAIN = new Color(248, 250, 252); // Slate 50
    public static final Color DARK_TEXT_SUB = new Color(148, 163, 184);  // Slate 400
    public static final Color DARK_BORDER = new Color(51, 65, 85);    // Slate 700

    // Light Mode Colors
    public static final Color LIGHT_BG = new Color(241, 245, 249);     // Slate 100
    public static final Color LIGHT_CARD = new Color(255, 255, 255);   // White
    public static final Color LIGHT_TEXT_MAIN = new Color(15, 23, 42); // Slate 900
    public static final Color LIGHT_TEXT_SUB = new Color(71, 85, 105); // Slate 600
    public static final Color LIGHT_BORDER = new Color(226, 232, 240); // Slate 200

    // Accents & State Colors (Universal)
    public static final Color ACCENT = new Color(99, 102, 241);        // Indigo 500
    public static final Color ACCENT_HOVER = new Color(79, 70, 229);  // Indigo 600
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);         // Red 500
    public static final Color SIDEBAR_BG = new Color(9, 15, 29);       // Darker Sidebar

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font CODE_FONT = new Font("Consolas", Font.PLAIN, 14);

    public static Color getBg() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }

    public static Color getCard() {
        return isDarkMode ? DARK_CARD : LIGHT_CARD;
    }

    public static Color getTextMain() {
        return isDarkMode ? DARK_TEXT_MAIN : LIGHT_TEXT_MAIN;
    }

    public static Color getTextSub() {
        return isDarkMode ? DARK_TEXT_SUB : LIGHT_TEXT_SUB;
    }

    public static Color getBorder() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }
}
