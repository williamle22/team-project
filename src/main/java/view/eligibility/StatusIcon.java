package view.eligibility;

import javax.swing.*;
import java.awt.*;

/** The {@link StatusBadge} dot as an {@link Icon}, for trees, lists, and labels. */
public final class StatusIcon implements Icon {
    private final String status;
    private final int size;

    public StatusIcon(String status, int size) {
        this.status = status;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        StatusBadge.paintDot(g, status, x, y, size);
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
