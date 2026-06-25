package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GradientPanel extends JPanel {
    private Color startColor;
    private Color endColor;
    private boolean horizontal = true;

    public GradientPanel(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        setOpaque(false);
    }

    public GradientPanel(Color startColor, Color endColor, boolean horizontal) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.horizontal = horizontal;
        setOpaque(false);
    }

    public void setColors(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        GradientPaint gp;
        if (horizontal) {
            gp = new GradientPaint(0, 0, startColor, w, 0, endColor);
        } else {
            gp = new GradientPaint(0, 0, startColor, 0, h, endColor);
        }
        
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
        
        super.paintComponent(g);
    }
}
