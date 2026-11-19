package view.dgx;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

/** A compact display tile for one planned course. */
public final class CourseBox extends JPanel {
    public CourseBox(String courseCode, TransferHandler dropHandler) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(new JLabel(courseCode));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x8A9BA8)), new EmptyBorder(2, 8, 2, 8)));
        setAlignmentY(Component.CENTER_ALIGNMENT);
        setTransferHandler(dropHandler);
    }
}
