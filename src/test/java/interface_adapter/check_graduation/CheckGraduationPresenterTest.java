package interface_adapter.check_graduation;

import entity.evaluation.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.check_graduation.CheckGraduationOutputData;
import use_case.check_graduation.ProgramSummary;
import use_case.check_graduation.RowLine;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckGraduationPresenterTest {
    private static final UUID PLAN = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private GraduationViewModel viewModel;
    private CheckGraduationPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new GraduationViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new CheckGraduationPresenter(viewModel);
    }

    private static RowLine line(String label, Status status, String reason, List<String> claimed,
                                RowLine... children) {
        return new RowLine(label, label + " text", status, reason, claimed, List.of(children));
    }

    private static RowLine leaf(String label, Status status) {
        return line(label, status, null, List.of());
    }

    private GraduationState present(Status overall, boolean projected, RowLine degree, List<ProgramSummary> programs) {
        presenter.prepareSuccessView(new CheckGraduationOutputData(PLAN, projected, overall, degree,
                leaf("Programs", MET), List.of("note"), programs));
        return viewModel.getState();
    }

    @Test
    void successFillsTheState() {
        GraduationState s = present(NOT_MET, true, leaf("Degree", NOT_MET), List.of());

        assertEquals(PLAN, s.getPlanId());
        assertTrue(s.isProjected());
        assertEquals("not met", s.getStatus());
        assertEquals(new GraduationLineView("Degree", "Degree text", "not met", "", "", List.of()), s.getDegree());
        assertEquals(new GraduationLineView("Programs", "Programs text", "met", "", "", List.of()),
                s.getCombination());
        assertEquals(List.of("note"), s.getNotes());
        assertEquals("", s.getError());
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertSame(s, events.get(0).getNewValue());
    }

    @Test
    void treeConvertsRecursively() {
        RowLine degree = line("Degree", REVIEW, null, List.of(),
                line("Requirement 1", NOT_MET, "1.5 of 3.0 credits", List.of("CSC108H1", "CSC148H1"),
                        leaf("Requirement 2", REVIEW)));

        GraduationLineView row = present(REVIEW, true, degree, List.of()).getDegree().getChildren().get(0);

        assertEquals(new GraduationLineView("Requirement 1", "Requirement 1 text", "not met", "1.5 of 3.0 credits",
                "CSC108H1, CSC148H1", List.of(new GraduationLineView("Requirement 2", "Requirement 2 text",
                "review", "", "", List.of()))), row);
    }

    @Test
    void cgpaUnknownExplainsMissingGrades() {
        RowLine degree = line("Degree", REVIEW, null, List.of(),
                line("Requirement 5", REVIEW, "needs a CGPA of 1.85; CGPA unknown", List.of()));

        GraduationLineView row = present(REVIEW, false, degree, List.of()).getDegree().getChildren().get(0);

        assertEquals("needs a CGPA of 1.85; CGPA unknown until you enter final grades", row.getReason());
    }

    @Test
    void dropsReasonThatRepeatsText() {
        RowLine degree = line("Degree", REVIEW, null, List.of(),
                new RowLine("Requirement 17", "Transfer credits only", REVIEW, "transfer credits only", List.of(),
                        List.of()));

        GraduationLineView row = present(REVIEW, true, degree, List.of()).getDegree().getChildren().get(0);

        assertEquals("", row.getReason());
    }

    @Test
    void programsConvertInOrder() {
        ProgramSummary a = new ProgramSummary("ASMAJ0001", "A Major", leaf("Enrolment", MET),
                leaf("Completion", REVIEW), List.of("a note"));
        ProgramSummary b = new ProgramSummary("ASMIN0002", "B Minor", leaf("Enrolment", REVIEW),
                leaf("Completion", NOT_MET), List.of());

        List<ProgramView> programs = present(REVIEW, true, leaf("Degree", MET), List.of(a, b)).getPrograms();

        assertEquals(new ProgramView("ASMAJ0001", "A Major", "review",
                new GraduationLineView("Enrolment", "Enrolment text", "met", "", "", List.of()),
                new GraduationLineView("Completion", "Completion text", "review", "", "", List.of()),
                List.of("a note")), programs.get(0));
        assertEquals("ASMIN0002", programs.get(1).getCode());
        assertEquals("not met", programs.get(1).getStatus());
    }

    @Test
    void summaryDependsOnStatusAndMode() {
        RowLine degree = leaf("Degree", MET);

        assertEquals("This plan meets every requirement", present(MET, true, degree, List.of()).getSummary());
        assertEquals("Ready to graduate", present(MET, false, degree, List.of()).getSummary());
        assertEquals("This plan doesn't meet every requirement",
                present(NOT_MET, true, degree, List.of()).getSummary());
        assertEquals("Not ready to graduate yet", present(NOT_MET, false, degree, List.of()).getSummary());
        assertEquals("Some requirements need review", present(REVIEW, true, degree, List.of()).getSummary());
        assertEquals("Some requirements need review", present(REVIEW, false, degree, List.of()).getSummary());
    }

    @Test
    void failSetsOnlyTheError() {
        presenter.prepareFailView("Choose a degree for this plan.");

        GraduationState s = viewModel.getState();
        GraduationState expected = new GraduationState();
        expected.setError("Choose a degree for this plan.");

        assertEquals(expected, s);
        assertNull(s.getDegree());
        assertEquals(1, events.size());
    }
}
