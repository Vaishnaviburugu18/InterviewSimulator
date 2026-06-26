package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Self-contained vector icon factory that paints modern, sharp icons.
 * This completely avoids using OS-dependent Unicode emojis which often render as square boxes (□).
 */
public class IconFactory {

    public static Icon getIcon(String name, int size) {
        return getIcon(name, size, null);
    }

    public static Icon getIcon(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    private static class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        public VectorIcon(String name, int size, Color color) {
            this.name = name.toLowerCase();
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            Color drawColor = color != null ? color : c.getForeground();
            g2.setColor(drawColor);
            g2.translate(x, y);

            double w = size;
            double h = size;

            switch (name) {
                case "home":
                    Path2D.Double home = new Path2D.Double();
                    home.moveTo(w / 2.0, h * 0.15);
                    home.lineTo(w * 0.15, h * 0.5);
                    home.lineTo(w * 0.25, h * 0.5);
                    home.lineTo(w * 0.25, h * 0.9);
                    home.lineTo(w * 0.75, h * 0.9);
                    home.lineTo(w * 0.75, h * 0.5);
                    home.lineTo(w * 0.85, h * 0.5);
                    home.closePath();
                    g2.fill(home);
                    break;

                case "domains":
                case "books":
                case "book":
                    // Stack of books or open book shape
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new RoundRectangle2D.Double(w * 0.15, h * 0.2, w * 0.7, h * 0.18, w * 0.05, h * 0.05));
                    g2.draw(new RoundRectangle2D.Double(w * 0.15, h * 0.42, w * 0.7, h * 0.18, w * 0.05, h * 0.05));
                    g2.draw(new RoundRectangle2D.Double(w * 0.15, h * 0.64, w * 0.7, h * 0.18, w * 0.05, h * 0.05));
                    break;

                case "analytics":
                case "chart":
                    // Bar chart shape
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Axis lines
                    g2.draw(new Line2D.Double(w * 0.15, h * 0.15, w * 0.15, h * 0.85));
                    g2.draw(new Line2D.Double(w * 0.15, h * 0.85, w * 0.85, h * 0.85));
                    // Bars
                    g2.fill(new Rectangle2D.Double(w * 0.28, h * 0.5, w * 0.12, h * 0.35));
                    g2.fill(new Rectangle2D.Double(w * 0.48, h * 0.3, w * 0.12, h * 0.55));
                    g2.fill(new Rectangle2D.Double(w * 0.68, h * 0.2, w * 0.12, h * 0.65));
                    break;

                case "history":
                case "list":
                    // 3 horizontal bullet-list items
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Bullets
                    g2.fill(new Ellipse2D.Double(w * 0.15, h * 0.23, w * 0.12, h * 0.12));
                    g2.fill(new Ellipse2D.Double(w * 0.15, h * 0.47, w * 0.12, h * 0.12));
                    g2.fill(new Ellipse2D.Double(w * 0.15, h * 0.71, w * 0.12, h * 0.12));
                    // Lines
                    g2.draw(new Line2D.Double(w * 0.38, h * 0.29, w * 0.85, h * 0.29));
                    g2.draw(new Line2D.Double(w * 0.38, h * 0.53, w * 0.85, h * 0.53));
                    g2.draw(new Line2D.Double(w * 0.38, h * 0.77, w * 0.85, h * 0.77));
                    break;

                case "profile":
                case "user":
                    // Head and shoulders outline
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Ellipse2D.Double(w * 0.35, h * 0.15, w * 0.3, h * 0.3));
                    g2.draw(new Arc2D.Double(w * 0.15, h * 0.55, w * 0.7, h * 0.7, 0, 180, Arc2D.OPEN));
                    break;

                case "admin":
                case "gear":
                case "settings":
                    // Gear icon
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Ellipse2D.Double(w * 0.28, h * 0.28, w * 0.44, h * 0.44));
                    g2.draw(new Ellipse2D.Double(w * 0.42, h * 0.42, w * 0.16, h * 0.16));
                    for (int angle = 0; angle < 360; angle += 45) {
                        double rad = Math.toRadians(angle);
                        double x1 = w / 2.0 + Math.cos(rad) * (w * 0.22);
                        double y1 = h / 2.0 + Math.sin(rad) * (h * 0.22);
                        double x2 = w / 2.0 + Math.cos(rad) * (w * 0.42);
                        double y2 = h / 2.0 + Math.sin(rad) * (h * 0.42);
                        g2.draw(new Line2D.Double(x1, y1, x2, y2));
                    }
                    break;

                case "logout":
                    // Power symbol / Exit bracket and arrow
                    g2.setStroke(new BasicStroke((float)(size * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Arc2D.Double(w * 0.15, h * 0.15, w * 0.52, h * 0.7, 90, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Double(w * 0.35, h / 2.0, w * 0.85, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.62, h * 0.27, w * 0.85, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.62, h * 0.73, w * 0.85, h / 2.0));
                    break;

                case "timer":
                case "clock":
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Ellipse2D.Double(w * 0.1, h * 0.1, w * 0.8, h * 0.8));
                    g2.draw(new Line2D.Double(w / 2.0, h / 2.0, w / 2.0, h * 0.28));
                    g2.draw(new Line2D.Double(w / 2.0, h / 2.0, w * 0.7, h / 2.0));
                    break;

                case "check":
                case "success":
                    g2.setStroke(new BasicStroke((float)(size * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Double(w * 0.18, h * 0.52, w * 0.42, h * 0.78));
                    g2.draw(new Line2D.Double(w * 0.42, h * 0.78, w * 0.85, h * 0.22));
                    break;

                case "close":
                case "danger":
                case "cross":
                    g2.setStroke(new BasicStroke((float)(size * 0.12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Double(w * 0.22, h * 0.22, w * 0.78, h * 0.78));
                    g2.draw(new Line2D.Double(w * 0.78, h * 0.22, w * 0.22, h * 0.78));
                    break;

                case "star":
                    Path2D.Double star = buildStarPath(w, h);
                    g2.fill(star);
                    break;

                case "star-empty":
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D.Double starEmpty = buildStarPath(w, h);
                    g2.draw(starEmpty);
                    break;

                case "search":
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Ellipse2D.Double(w * 0.15, h * 0.15, w * 0.48, h * 0.48));
                    g2.draw(new Line2D.Double(w * 0.52, h * 0.52, w * 0.85, h * 0.85));
                    break;

                case "arrow-left":
                case "prev":
                    g2.setStroke(new BasicStroke((float)(size * 0.1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Double(w * 0.8, h / 2.0, w * 0.2, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.48, h * 0.22, w * 0.2, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.48, h * 0.78, w * 0.2, h / 2.0));
                    break;

                case "arrow-right":
                case "next":
                    g2.setStroke(new BasicStroke((float)(size * 0.1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Double(w * 0.2, h / 2.0, w * 0.8, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.52, h * 0.22, w * 0.8, h / 2.0));
                    g2.draw(new Line2D.Double(w * 0.52, h * 0.78, w * 0.8, h / 2.0));
                    break;

                case "refresh":
                case "retry":
                    g2.setStroke(new BasicStroke((float)(size * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Arc2D.Double(w * 0.15, h * 0.15, w * 0.7, h * 0.7, 45, 270, Arc2D.OPEN));
                    // Arrow tip
                    Path2D.Double tip = new Path2D.Double();
                    tip.moveTo(w * 0.65, h * 0.05);
                    tip.lineTo(w * 0.85, h * 0.25);
                    tip.lineTo(w * 0.58, h * 0.35);
                    g2.fill(tip);
                    break;

                case "trophy":
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Cup
                    g2.draw(new Arc2D.Double(w * 0.22, h * 0.15, w * 0.56, h * 0.46, 180, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Double(w * 0.22, h * 0.15, w * 0.78, h * 0.15));
                    // Stem & base
                    g2.draw(new Line2D.Double(w / 2.0, h * 0.61, w / 2.0, h * 0.8));
                    g2.draw(new Line2D.Double(w * 0.3, h * 0.8, w * 0.7, h * 0.8));
                    // Handles
                    g2.draw(new Arc2D.Double(w * 0.12, h * 0.22, w * 0.2, h * 0.24, 90, 180, Arc2D.OPEN));
                    g2.draw(new Arc2D.Double(w * 0.68, h * 0.22, w * 0.2, h * 0.24, 270, 180, Arc2D.OPEN));
                    break;

                case "lock":
                    g2.setStroke(new BasicStroke((float)(size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    // Shackle
                    g2.draw(new Arc2D.Double(w * 0.26, h * 0.16, w * 0.48, h * 0.5, 0, 180, Arc2D.OPEN));
                    // Body
                    g2.fill(new RoundRectangle2D.Double(w * 0.2, h * 0.45, w * 0.6, h * 0.4, w * 0.08, h * 0.08));
                    break;

                case "warning":
                    g2.setStroke(new BasicStroke((float)(size * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D.Double tri = new Path2D.Double();
                    tri.moveTo(w / 2.0, h * 0.12);
                    tri.lineTo(w * 0.12, h * 0.85);
                    tri.lineTo(w * 0.88, h * 0.85);
                    tri.closePath();
                    g2.draw(tri);
                    // Exclamation point
                    g2.fill(new Rectangle2D.Double(w * 0.46, h * 0.38, w * 0.08, h * 0.22));
                    g2.fill(new Ellipse2D.Double(w * 0.45, h * 0.68, w * 0.1, h * 0.1));
                    break;

                default:
                    // Fallback to simple circle
                    g2.fill(new Ellipse2D.Double(w * 0.25, h * 0.25, w * 0.5, h * 0.5));
                    break;
            }

            g2.dispose();
        }

        private Path2D.Double buildStarPath(double w, double h) {
            Path2D.Double path = new Path2D.Double();
            double midX = w / 2.0;
            double midY = h / 2.0 + (h * 0.04); // Adjust center slightly down
            double rOuter = w * 0.46;
            double rInner = w * 0.18;
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? rOuter : rInner;
                double angle = Math.toRadians(i * 36 - 90);
                double px = midX + Math.cos(angle) * r;
                double py = midY + Math.sin(angle) * r;
                if (i == 0) path.moveTo(px, py);
                else path.lineTo(px, py);
            }
            path.closePath();
            return path;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
