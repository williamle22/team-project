package view.dgx;

import interface_adapter.dgx.AcademicPlannerState;
import interface_adapter.dgx.AcademicPlannerState.TermRow;
import interface_adapter.dgx.AcademicPlannerViewModel;
import interface_adapter.dgx.AddCourseToPlanController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
            termsPanel.add(new TermPanel(term, controller));
            termsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        if (state.getTerms().isEmpty()) {
            termsPanel.add(new JLabel("No Fall or Winter terms in this plan."));
        }
        termsPanel.revalidate();
        termsPanel.repaint();
    }
}
