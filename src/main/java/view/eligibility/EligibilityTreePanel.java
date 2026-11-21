package view.eligibility;

import interface_adapter.check_eligibility.CourseEligibilityView;
import interface_adapter.check_eligibility.EligibilityLineView;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

/**
 * Shows one course's eligibility: a heading with its status and summary, its warnings, and its requirement tree with
 * every row expanded.
 */
public final class EligibilityTreePanel extends JPanel {
    private static final int ICON_SIZE = 10;

    private final StatusBadge badge = new StatusBadge();
    private final JLabel title = new JLabel();
    private final JLabel summary = new JLabel();
    private final WarningList warnings = new WarningList();
    private final JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));

    public EligibilityTreePanel() {
        super(new BorderLayout(0, 8));

        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 2f));
        summary.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        heading.add(badge);
        heading.add(title);
        heading.add(summary);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(heading, BorderLayout.NORTH);
        top.add(warnings, BorderLayout.CENTER);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new LineRenderer());

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        clear();
    }

    public void show(CourseEligibilityView course) {
        badge.setStatus(course.getStatus());
        badge.setVisible(true);
        title.setText(course.getCode());
        summary.setText(course.getSummary());
        warnings.setWarnings(course.getWarnings());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        addAll(root, course.getLines());
        tree.setModel(new DefaultTreeModel(root));

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /** Shows a prompt to select a course. */
    public void clear() {
        badge.setVisible(false);
        title.setText("Select a course");
        summary.setText("");
        warnings.setWarnings(List.of());
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    }

    JTree getTree() {
        return tree;
    }

    private static void addAll(DefaultMutableTreeNode parent, List<EligibilityLineView> lines) {
        for (EligibilityLineView line : lines) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(line);
            addAll(node, line.getChildren());
            parent.add(node);
        }
    }

    static String html(EligibilityLineView line, Color reasonColour) {
        String text = WarningList.escape(line.getText());

        if (line.getReason().isEmpty()) {
            return "<html>" + text + "</html>";
        }

        String colour = String.format("#%06x", reasonColour.getRGB() & 0xFFFFFF);

        return "<html>" + text + " <font color='" + colour + "'>" + WarningList.escape(line.getReason())
                + "</font></html>";
    }

    private static final class LineRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            Object line = ((DefaultMutableTreeNode) value).getUserObject();

            if (line instanceof EligibilityLineView) {
                EligibilityLineView v = (EligibilityLineView) line;
                setIcon(new StatusIcon(v.getStatus(), ICON_SIZE));
                setText(html(v, UIManager.getColor("Label.disabledForeground")));
            }

            return this;
        }
    }
}
