package view.eligibility;

import javax.swing.*;
import java.awt.*;

public final class StatusBadge extends JComponent {
    private static final int SIZE = 12;
    private static final Color MET = new Color(0x2E7D32);
    private static final Color NOT_MET = new Color(0xC62828);
    private static final Color REVIEW = new Color(0xF9A825);
    private static final Color UNKNOWN = new Color(0x9E9E9E);

    private String status = "";

    public void setStatus(String status) {
        this.status = status;
        setToolTipText(tooltipFor(status));
        repaint();
    }

    public String getStatus() {
        return status;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZE, SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int size = Math.min(getWidth(), getHeight());
        paintDot(g, status, (getWidth() - size) / 2, (getHeight() - size) / 2, size);
    }

    public static Color colourFor(String status) {
        switch (status) {
            case "met":
                return MET;
            case "not met":
                return NOT_MET;
            case "review":
                return REVIEW;
            default:
                return UNKNOWN;
        }
    }

    public static String tooltipFor(String status) {
        switch (status) {
            case "met":
                return "Requirements met";
            case "not met":
                return "Requirements not met";
            case "review":
                return "Needs review";
            default:
                return null;
        }
    }

    static void paintDot(Graphics g, String status, int x, int y, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colourFor(status));
        g2.fillOval(x, y, size, size);
        g2.dispose();
    }
}
