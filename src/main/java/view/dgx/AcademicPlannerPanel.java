package view.dgx;

import interface_adapter.dgx.AcademicPlannerState;
import interface_adapter.dgx.AcademicPlannerState.CourseRow;
import interface_adapter.dgx.AcademicPlannerState.TermRow;
import interface_adapter.dgx.AcademicPlannerViewModel;
import interface_adapter.dgx.AddCourseToPlanController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/** Displays plan terms and accepts dragged course codes on Fall and Winter terms. */
public class AcademicPlannerPanel extends JPanel implements PropertyChangeListener {
    private final AcademicPlannerViewModel viewModel;
    private final AddCourseToPlanController controller;
    private final JLabel message = new JLabel(" ");
    private final JPanel termsPanel = new JPanel();

    public AcademicPlannerPanel(AcademicPlannerViewModel viewModel, AddCourseToPlanController controller) {
        super(new BorderLayout(8, 8));
        this.viewModel = viewModel;
        this.controller = controller;

        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("Academic Planner"), BorderLayout.NORTH);
        header.add(message, BorderLayout.SOUTH);

        termsPanel.setLayout(new BoxLayout(termsPanel, BoxLayout.Y_AXIS));
        termsPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        viewModel.addPropertyChangeListener(this);
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(termsPanel), BorderLayout.CENTER);
        redraw(viewModel.getState());
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(event));
            return;
        }
        redraw((AcademicPlannerState) event.getNewValue());
    }

    private void redraw(AcademicPlannerState state) {
        message.setText(state.getMessage().isEmpty() ? " " : state.getMessage());
        termsPanel.removeAll();
        for (TermRow term : state.getTerms()) {
            termsPanel.add(createTermPanel(term));
            termsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        if (state.getTerms().isEmpty()) {
            termsPanel.add(new JLabel("No Fall or Winter terms in this plan."));
        }
        termsPanel.revalidate();
        termsPanel.repaint();
    }

    private JPanel createTermPanel(TermRow term) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(new TitledBorder(term.getLabel()));
        TransferHandler dropHandler = new TransferHandler() {
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

        panel.setTransferHandler(dropHandler);
        for (CourseRow course : term.getCourses()) {
            JPanel courseBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            courseBox.add(new JLabel(course.getCode()));
            courseBox.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(0x8A9BA8)), new EmptyBorder(2, 8, 2, 8)));
            courseBox.setAlignmentY(Component.CENTER_ALIGNMENT);
            courseBox.setTransferHandler(dropHandler);
            panel.add(courseBox);
        }
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }
}
