package ui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CircularScoreIndicator extends JComponent {
    private int score = 0; // 0 to 100
    private int thickness = 14;

    public CircularScoreIndicator(int size) {
        setPreferredSize(new Dimension(size, size));
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int padding = thickness + 4;
        int d = size - padding * 2;
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;

        // Draw track
        g2.setColor(Theme.isDarkMode ? new Color(51, 65, 85) : new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(x, y, d, d, 0, 360);

        // Determine arc color
        Color arcColor;
        if (score >= 85) {
            arcColor = Theme.SUCCESS;
        } else if (score >= 60) {
            arcColor = Theme.ACCENT;
        } else if (score >= 40) {
            arcColor = Theme.WARNING;
        } else {
            arcColor = Theme.DANGER;
        }

        // Draw active arc (starts at top - 90 deg, negative is clockwise)
        g2.setColor(arcColor);
        int angle = (int) (-score * 3.6);
        g2.drawArc(x, y, d, d, 90, angle);

        // Draw center text
        String text = score + "%";
        Font f = Theme.TITLE_FONT.deriveFont(Font.BOLD, size * 0.2f);
        g2.setFont(f);
        g2.setColor(Theme.getTextMain());
        FontMetrics fm = g2.getFontMetrics(f);
        int tx = (getWidth() - fm.stringWidth(text)) / 2;
        int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
