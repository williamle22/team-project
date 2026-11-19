package view.dgx;

import interface_adapter.dgx.AcademicPlannerState.CourseRow;
import interface_adapter.dgx.AcademicPlannerState.TermRow;
import interface_adapter.dgx.AddCourseToPlanController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

/** A single Fall or Winter term drop target with its planned courses. */
public final class TermPanel extends JPanel {
    public TermPanel(TermRow term, AddCourseToPlanController controller) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setBorder(new TitledBorder(term.getLabel()));

        TransferHandler dropHandler = createDropHandler(term, controller);
        setTransferHandler(dropHandler);
        for (CourseRow course : term.getCourses()) {
            JPanel courseBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            courseBox.add(new JLabel(course.getCode()));
            courseBox.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0x8A9BA8)), new EmptyBorder(2, 8, 2, 8)));
            courseBox.setAlignmentY(Component.CENTER_ALIGNMENT);
            courseBox.setTransferHandler(dropHandler);
            add(courseBox);
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
    }

    private TransferHandler createDropHandler(TermRow term, AddCourseToPlanController controller) {
        return new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    Transferable transferable = support.getTransferable();
                    String courseCode = ((String) transferable.getTransferData(DataFlavor.stringFlavor)).trim();
                    if (courseCode.isEmpty()) {
                        return false;
                    }
                    controller.execute(courseCode, term.getYear(), term.getSeason());
                    return true;
                } catch (UnsupportedOperationException | IllegalArgumentException e) {
                    return false;
                } catch (java.awt.datatransfer.UnsupportedFlavorException | IOException e) {
                    return false;
                }
            }
        };
    }
}
