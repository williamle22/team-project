package view.graduation;

import interface_adapter.check_graduation.CheckGraduationController;
import interface_adapter.check_graduation.GraduationLineView;
import interface_adapter.check_graduation.GraduationState;
import interface_adapter.check_graduation.GraduationViewModel;
import interface_adapter.check_graduation.ProgramView;
import view.eligibility.StatusBadge;
import view.eligibility.StatusIcon;
import view.eligibility.WarningList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;

/**
 * Shows a plan's graduation check: the overall status and summary, a toggle between completed courses only and the
 * whole plan, the plan's notes, and a fully expanded tree with the degree, the program combination, and each program.
 */
public final class GraduationPanel extends JPanel implements PropertyChangeListener {
    private static final int ICON_SIZE = 10;

    private final CheckGraduationController controller;
    private final StatusBadge badge = new StatusBadge();
    private final JLabel summary = new JLabel();
    private final JRadioButton completedOnly = new JRadioButton("Completed only", true);
    private final JRadioButton wholePlan = new JRadioButton("Whole plan");
    private final WarningList notes = new WarningList();
    private final JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
    private UUID planId;

    public GraduationPanel(GraduationViewModel viewModel, CheckGraduationController controller) {
        super(new BorderLayout(0, 8));
        this.controller = controller;

        summary.setFont(summary.getFont().deriveFont(Font.BOLD, summary.getFont().getSize2D() + 2f));

        ButtonGroup mode = new ButtonGroup();
        mode.add(completedOnly);
        mode.add(wholePlan);
        completedOnly.addActionListener(e -> recheck());
        wholePlan.addActionListener(e -> recheck());

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        heading.add(badge);
        heading.add(summary);
        heading.add(completedOnly);
        heading.add(wholePlan);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(heading, BorderLayout.NORTH);
        top.add(notes, BorderLayout.CENTER);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new LineRenderer());

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        viewModel.addPropertyChangeListener(this);
        show(viewModel.getState());
    }

    /** Checks the plan, and checks it again whenever the student changes the toggle. */
    public void check(UUID planId) {
        this.planId = planId;
        recheck();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(evt));
            return;
        }

        show((GraduationState) evt.getNewValue());
    }

    JTree getTree() {
        return tree;
    }

    JLabel getSummary() {
        return summary;
    }

    JRadioButton getWholePlan() {
        return wholePlan;
    }

    private void recheck() {
        if (planId != null) {
            controller.execute(planId, wholePlan.isSelected());
        }
    }

    private void show(GraduationState state) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        if (state.getDegree() == null) {
            badge.setVisible(false);
            summary.setText(state.getError().isEmpty() ? "Select a plan" : state.getError());
            notes.setWarnings(List.of());
            tree.setModel(new DefaultTreeModel(root));
            return;
        }

        badge.setStatus(state.getStatus());
        badge.setVisible(true);
        summary.setText(state.getSummary());
        notes.setWarnings(state.getNotes());

        root.add(node(state.getDegree()));
        root.add(node(state.getCombination()));

        for (ProgramView p : state.getPrograms()) {
            DefaultMutableTreeNode program = new DefaultMutableTreeNode(p);
            program.add(node(p.getEnrolment()));
            program.add(node(p.getCompletion()));

            for (String note : p.getNotes()) {
                program.add(new DefaultMutableTreeNode(note));
            }

            root.add(program);
        }

        tree.setModel(new DefaultTreeModel(root));

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private static DefaultMutableTreeNode node(GraduationLineView line) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(line);

        for (GraduationLineView child : line.getChildren()) {
            node.add(node(child));
        }

        return node;
    }

    static String html(GraduationLineView line, Color grey) {
        String colour = String.format("#%06x", grey.getRGB() & 0xFFFFFF);
        StringBuilder sb = new StringBuilder("<html><b>").append(escape(line.getLabel())).append("</b> ")
                .append(escape(line.getText()));

        if (!line.getReason().isEmpty()) {
            sb.append(" <font color='").append(colour).append("'>").append(escape(line.getReason()))
                    .append("</font>");
        }

        if (!line.getClaimed().isEmpty()) {
            sb.append(" <font color='").append(colour).append("'>(").append(escape(line.getClaimed()))
                    .append(")</font>");
        }

        return sb.append("</html>").toString();
    }

    static String html(ProgramView p) {
        String name = p.getTitle().equals(p.getCode()) ? p.getCode() : p.getTitle() + " " + p.getCode();
        return "<html><b>" + escape(name) + "</b></html>";
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class LineRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            Object item = ((DefaultMutableTreeNode) value).getUserObject();

            if (item instanceof GraduationLineView) {
                GraduationLineView v = (GraduationLineView) item;
                setIcon(new StatusIcon(v.getStatus(), ICON_SIZE));
                setText(html(v, UIManager.getColor("Label.disabledForeground")));
            } else if (item instanceof ProgramView) {
                ProgramView p = (ProgramView) item;
                setIcon(new StatusIcon(p.getStatus(), ICON_SIZE));
                setText(html(p));
            } else {
                setIcon(null);
            }

            return this;
        }
    }
}
