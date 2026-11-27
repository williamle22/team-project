package view.graduation;

import interface_adapter.check_graduation.CheckGraduationController;
import interface_adapter.check_graduation.GraduationLineView;
import interface_adapter.check_graduation.GraduationState;
import interface_adapter.check_graduation.GraduationViewModel;
import interface_adapter.check_graduation.ProgramView;
import org.junit.jupiter.api.Test;
import use_case.check_graduation.CheckGraduationInputData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduationPanelTest {
    private static final UUID PLAN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final GraduationLineView REQUIREMENT_2 =
            new GraduationLineView("Requirement 2", "1.0 credit at the 300+ level", "met", "", "", List.of());
    private static final GraduationLineView REQUIREMENT_1 = new GraduationLineView("Requirement 1", "3.0 credits",
            "not met", "1.5 of 3.0 credits", "CSC108H1", List.of(REQUIREMENT_2));
    private static final GraduationLineView DEGREE = new GraduationLineView("Degree", "Honours Bachelor of Science",
            "not met", "", "", List.of(REQUIREMENT_1));
    private static final GraduationLineView COMBINATION =
            new GraduationLineView("Programs", "2 Majors", "not met", "needs 2 Majors completed", "", List.of());
    private static final ProgramView PROGRAM = new ProgramView("ASMAJ1689", "Computer Science Major", "review",
            new GraduationLineView("Enrolment", "Open enrolment.", "met", "", "", List.of()),
            new GraduationLineView("Completion", "Completion requirements", "review", "", "", List.of()),
            List.of("a note"));

    private final List<CheckGraduationInputData> inputs = new ArrayList<>();
    private final GraduationViewModel viewModel = new GraduationViewModel();
    private final GraduationPanel panel = new GraduationPanel(viewModel, new CheckGraduationController(inputs::add));

    private void publish(GraduationState state) throws InterruptedException, InvocationTargetException {
        viewModel.setState(state);
        viewModel.firePropertyChanged();
        SwingUtilities.invokeAndWait(() -> { });
    }

    private static GraduationState success() {
        GraduationState s = new GraduationState();
        s.setPlanId(PLAN);
        s.setStatus("not met");
        s.setSummary("Not ready to graduate yet");
        s.setDegree(DEGREE);
        s.setCombination(COMBINATION);
        s.setNotes(List.of("degree note"));
        s.setPrograms(List.of(PROGRAM));
        return s;
    }

    private static Object row(JTree tree, int row) {
        return ((DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent()).getUserObject();
    }

    @Test
    void startsAskingForAPlan() {
        assertEquals("Select a plan", panel.getSummary().getText());
        assertEquals(0, panel.getTree().getRowCount());
    }

    @Test
    void showsTheError() throws Exception {
        GraduationState s = new GraduationState();
        s.setError("Choose a degree for this plan.");

        publish(s);

        assertEquals("Choose a degree for this plan.", panel.getSummary().getText());
        assertEquals(0, panel.getTree().getRowCount());
    }

    @Test
    void successBuildsExpandedTree() throws Exception {
        publish(success());

        JTree tree = panel.getTree();
        assertEquals("Not ready to graduate yet", panel.getSummary().getText());
        assertEquals(List.of(DEGREE, REQUIREMENT_1, REQUIREMENT_2, COMBINATION, PROGRAM, PROGRAM.getEnrolment(),
                PROGRAM.getCompletion(), "a note"), List.of(row(tree, 0), row(tree, 1), row(tree, 2), row(tree, 3),
                row(tree, 4), row(tree, 5), row(tree, 6), row(tree, 7)));
        assertEquals(8, tree.getRowCount());
    }

    @Test
    void errorAfterSuccessClearsTheTree() throws Exception {
        publish(success());
        GraduationState s = new GraduationState();
        s.setError("That plan no longer exists.");

        publish(s);

        assertEquals(0, panel.getTree().getRowCount());
    }

    @Test
    void checkRunsCompletedOnlyFirst() {
        panel.check(PLAN);

        assertEquals(List.of(new CheckGraduationInputData(PLAN, false)), inputs);
    }

    @Test
    void toggleRechecksTheSamePlan() {
        panel.check(PLAN);

        panel.getWholePlan().doClick(0);

        assertEquals(List.of(new CheckGraduationInputData(PLAN, false), new CheckGraduationInputData(PLAN, true)),
                inputs);
    }

    @Test
    void toggleBeforeAPlanDoesNothing() {
        panel.getWholePlan().doClick(0);

        assertTrue(inputs.isEmpty());
    }

    @Test
    void htmlShowsReasonAndClaimedInGrey() {
        assertEquals("<html><b>Requirement 1</b> 3.0 credits <font color='#808080'>1.5 of 3.0 credits</font> "
                + "<font color='#808080'>(CSC108H1)</font></html>", GraduationPanel.html(REQUIREMENT_1, Color.GRAY));
        assertEquals("<html><b>Requirement 2</b> 1.0 credit at the 300+ level</html>",
                GraduationPanel.html(REQUIREMENT_2, Color.GRAY));
    }

    @Test
    void htmlEscapesText() {
        GraduationLineView line = new GraduationLineView("A & B", "<x>", "met", "", "", List.of());

        assertEquals("<html><b>A &amp; B</b> &lt;x&gt;</html>", GraduationPanel.html(line, Color.GRAY));
    }

    @Test
    void programNameShowsCodeOnce() {
        ProgramView unknown = new ProgramView("ASMAJ9999", "ASMAJ9999", "review", PROGRAM.getEnrolment(),
                PROGRAM.getCompletion(), List.of());

        assertEquals("<html><b>Computer Science Major ASMAJ1689</b></html>", GraduationPanel.html(PROGRAM));
        assertEquals("<html><b>ASMAJ9999</b></html>", GraduationPanel.html(unknown));
    }

    @Test
    void rendererDrawsEveryKindOfRow() throws Exception {
        publish(success());
        JTree tree = panel.getTree();

        for (int row = 0; row < tree.getRowCount(); row++) {
            Object value = tree.getPathForRow(row).getLastPathComponent();
            Component c = tree.getCellRenderer().getTreeCellRendererComponent(tree, value, false, true, false, row,
                    false);
            JLabel label = (JLabel) c;
            String expectedIcon = row(tree, row) instanceof String ? "none" : "status";

            assertEquals(expectedIcon, label.getIcon() == null ? "none" : "status", "row " + row);
        }
    }
}
