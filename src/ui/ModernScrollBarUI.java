package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {
    
    private final int THUMB_SIZE = 8;
    private final Color TRACK_COLOR = new Color(30, 41, 59);
    private final Color THUMB_COLOR = new Color(71, 85, 105);
    private final Color THUMB_HOVER_COLOR = new Color(100, 116, 139);

    @Override
    protected void configureScrollBarColors() {
        this.trackColor = TRACK_COLOR;
        this.thumbColor = THUMB_COLOR;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(TRACK_COLOR);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isHovered = isThumbRollover();
        g2.setColor(isHovered ? THUMB_HOVER_COLOR : THUMB_COLOR);

        int arc = THUMB_SIZE;
        int x = thumbBounds.x;
        int y = thumbBounds.y;
        int width = thumbBounds.width;
        int height = thumbBounds.height;

        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            x += (width - THUMB_SIZE) / 2;
            width = THUMB_SIZE;
        } else {
            y += (height - THUMB_SIZE) / 2;
            height = THUMB_SIZE;
        }

        // Add small padding
        x += 2;
        y += 2;
        width -= 4;
        height -= 4;

        if (width > 0 && height > 0) {
            g2.fillRoundRect(x, y, width, height, arc, arc);
        }

        g2.dispose();
    }
}
