package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 15;
    private Color bgOverride = null;
    private Color borderOverride = null;
    private int borderThickness = 1;

    public RoundedPanel() {
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        this.cornerRadius = radius;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgOverride) {
        this.cornerRadius = radius;
        this.bgOverride = bgOverride;
        setOpaque(false);
    }

    public void setBgOverride(Color color) {
        this.bgOverride = color;
        repaint();
    }

    public void setBorderOverride(Color color, int thickness) {
        this.borderOverride = color;
        this.borderThickness = thickness;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color bg = (bgOverride != null) ? bgOverride : getBackground();
        g2.setColor(bg);
        
        // Draw the rounded background
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        
        // Draw the border if set
        if (borderOverride != null) {
            g2.setColor(borderOverride);
            g2.setStroke(new java.awt.BasicStroke(borderThickness));
            g2.drawRoundRect(borderThickness / 2, borderThickness / 2, 
                    getWidth() - borderThickness - 1, getHeight() - borderThickness - 1, 
                    cornerRadius, cornerRadius);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
}
