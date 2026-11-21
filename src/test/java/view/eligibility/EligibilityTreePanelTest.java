package view.eligibility;

import interface_adapter.check_eligibility.CourseEligibilityView;
import interface_adapter.check_eligibility.EligibilityLineView;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EligibilityTreePanelTest {
    private static final CourseEligibilityView CSC207 = new CourseEligibilityView(UUID.randomUUID(), "CSC207H1",
            "not met", "Missing CSC148H1", List.of(
            new EligibilityLineView("Prerequisites", "not met", "", List.of(
                    new EligibilityLineView("One of:", "not met", "", List.of(
                            new EligibilityLineView("CSC148H1", "not met", "not taken", List.of()),
                            new EligibilityLineView("CSC111H1", "not met", "not taken", List.of()))))),
            new EligibilityLineView("Exclusions", "met", "", List.of())), List.of());

    private static String rowText(JTree tree, int row) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent();
        return ((EligibilityLineView) node.getUserObject()).getText();
    }

    @Test
    void showBuildsExpandedTree() {
        EligibilityTreePanel panel = new EligibilityTreePanel();

        panel.show(CSC207);

        JTree tree = panel.getTree();
        assertEquals(5, tree.getRowCount());
        assertEquals("Prerequisites", rowText(tree, 0));
        assertEquals("One of:", rowText(tree, 1));
        assertEquals("CSC148H1", rowText(tree, 2));
        assertEquals("CSC111H1", rowText(tree, 3));
        assertEquals("Exclusions", rowText(tree, 4));
    }

    @Test
    void clearEmptiesTree() {
        EligibilityTreePanel panel = new EligibilityTreePanel();
        panel.show(CSC207);

        panel.clear();

        assertEquals(0, panel.getTree().getRowCount());
    }

    @Test
    void htmlAddsReasonInColour() {
        EligibilityLineView withReason = new EligibilityLineView("CSC148H1 (min. 60%)", "not met", "you have <55%",
                List.of());
        EligibilityLineView withoutReason = new EligibilityLineView("A & B", "met", "", List.of());

        assertEquals("<html>CSC148H1 (min. 60%) <font color='#808080'>you have &lt;55%</font></html>",
                EligibilityTreePanel.html(withReason, new Color(0x808080)));
        assertEquals("<html>A &amp; B</html>", EligibilityTreePanel.html(withoutReason, Color.GRAY));
    }
}
