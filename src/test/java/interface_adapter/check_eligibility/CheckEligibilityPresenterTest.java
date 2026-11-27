package interface_adapter.check_eligibility;

import entity.evaluation.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.check_eligibility.CheckEligibilityOutputData;
import use_case.check_eligibility.CourseEligibility;
import use_case.check_eligibility.EligibilityLine;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckEligibilityPresenterTest {
    private static final UUID PLAN = UUID.randomUUID();

    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private EligibilityViewModel viewModel;
    private CheckEligibilityPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new EligibilityViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new CheckEligibilityPresenter(viewModel);
    }

    private static EligibilityLine line(String text, Status status, String reason, EligibilityLine... children) {
        return new EligibilityLine(text, status, reason, List.of(children));
    }

    @Test
    void dropsReasonThatRepeatsText() {
        CourseEligibility c = course(REVIEW,
                line("Prerequisites", REVIEW, null,
                        line("Permission of the instructor", REVIEW, "permission of the instructor")));

        CourseEligibilityView v = present(c);

        assertEquals("", v.getLines().get(0).getChildren().get(0).getReason());
        assertEquals("Needs review: permission of the instructor", v.getSummary());
    }

    @Test
    void passesCourseWarnings() {
        CourseEligibility c = new CourseEligibility(UUID.randomUUID(), "CSC207H1", "Winter 2027", NOT_MET,
                List.of(line("Not offered in Winter 2027", NOT_MET, null)), List.of("Not offered in Winter 2027"));

        assertEquals(List.of("Not offered in Winter 2027"), present(c).getWarnings());
    }

    private static CourseEligibility course(Status overall, EligibilityLine... sections) {
        return new CourseEligibility(UUID.randomUUID(), "CSC207H1", "Fall 2026", overall, List.of(sections),
                List.of());
    }

    private CourseEligibilityView present(CourseEligibility c) {
        presenter.prepareSuccessView(new CheckEligibilityOutputData(PLAN, Map.of(c.getPlannedCourseId(), c),
                List.of()));
        return viewModel.getState().getCourses().get(c.getPlannedCourseId());
    }

    @Test
    void mapsStatuses() {
        CourseEligibility c = course(NOT_MET,
                line("Prerequisites", NOT_MET, null, line("CSC148H1", NOT_MET, "not taken")),
                line("Corequisites", MET, null, line("No corequisites", MET, null)),
                line("Exclusions", REVIEW, null, line("Placement at the FSL400+ level", REVIEW, null)));

        CourseEligibilityView v = present(c);

        assertEquals(c.getPlannedCourseId(), v.getPlannedCourseId());
        assertEquals("CSC207H1", v.getCode());
        assertEquals("not met", v.getStatus());
        assertEquals(List.of(
                new EligibilityLineView("Prerequisites", "not met", "", List.of(
                        new EligibilityLineView("CSC148H1", "not met", "not taken", List.of()))),
                new EligibilityLineView("Corequisites", "met", "", List.of(
                        new EligibilityLineView("No corequisites", "met", "", List.of()))),
                new EligibilityLineView("Exclusions", "review", "", List.of(
                        new EligibilityLineView("Placement at the FSL400+ level", "review", "", List.of())))),
                v.getLines());
    }

    @Test
    void summaryForMet() {
        assertEquals("Requirements met", present(course(MET, line("Prerequisites", MET, null))).getSummary());
    }

    @Test
    void summaryForMissingCourse() {
        CourseEligibility c = course(NOT_MET,
                line("Prerequisites", NOT_MET, null,
                        line("All of:", NOT_MET, null,
                                line("CSC108H1", MET, null),
                                line("CSC148H1 (min. 60%)", NOT_MET, "CSC148H1 not completed before Fall 2026"))));

        assertEquals("Missing CSC148H1 (min. 60%)", present(c).getSummary());
    }

    @Test
    void summaryForLowGradeUsesReason() {
        CourseEligibility c = course(NOT_MET,
                line("Prerequisites", NOT_MET, null,
                        line("CSC148H1 (min. 60%)", NOT_MET, "needs at least 60% in CSC148H1; you have 55%")));

        assertEquals("Needs at least 60% in CSC148H1; you have 55%", present(c).getSummary());
    }

    @Test
    void summaryForNotMetLineUsesReason() {
        CourseEligibility c = course(NOT_MET,
                line("Prerequisites", MET, null, line("CSC108H1", MET, null)),
                line("Exclusions", MET, null),
                line("Corequisites", NOT_MET, null,
                        line("None of:", NOT_MET, "can't have CSC111H1", line("CSC111H1", MET, null))));

        assertEquals("Can't have CSC111H1", present(c).getSummary());
    }

    @Test
    void summaryForNotMetWithoutReasonUsesText() {
        CourseEligibility c = course(NOT_MET, line("Not offered in Winter 2027", NOT_MET, null));

        assertEquals("Not offered in Winter 2027", present(c).getSummary());
    }

    @Test
    void summaryForReview() {
        CourseEligibility c = course(REVIEW,
                line("Prerequisites", REVIEW, null,
                        line("Permission of the instructor", REVIEW, "permission of the instructor")));

        assertEquals("Needs review: permission of the instructor", present(c).getSummary());
    }

    @Test
    void summaryForReviewWithoutReasonUsesText() {
        CourseEligibility c = course(REVIEW, line("Not in the current calendar", REVIEW, null));

        assertEquals("Needs review: not in the current calendar", present(c).getSummary());
    }

    @Test
    void summaryForReviewKeepsLeadingAcronym() {
        CourseEligibility c = course(REVIEW, line("FSL placement test", REVIEW, null));

        assertEquals("Needs review: FSL placement test", present(c).getSummary());
    }

    @Test
    void summaryHandlesShortText() {
        assertEquals("Needs review: X", present(course(REVIEW, line("X", REVIEW, null))).getSummary());
        assertEquals("", present(course(NOT_MET, line("", NOT_MET, null))).getSummary());
    }

    @Test
    void summaryWithoutMatchingLine() {
        assertEquals("Requirements not met", present(course(NOT_MET)).getSummary());
        assertEquals("Needs review", present(course(REVIEW)).getSummary());
    }

    @Test
    void successSetsStateAndFires() {
        CourseEligibility first = course(MET);
        CourseEligibility second = course(MET);
        Map<UUID, CourseEligibility> courses = new LinkedHashMap<>();
        courses.put(first.getPlannedCourseId(), first);
        courses.put(second.getPlannedCourseId(), second);

        presenter.prepareSuccessView(new CheckEligibilityOutputData(PLAN, courses,
                List.of("CSC108H1 appears 2 times (retake?)")));

        EligibilityState s = viewModel.getState();
        assertEquals(PLAN, s.getPlanId());
        assertEquals(List.of(first.getPlannedCourseId(), second.getPlannedCourseId()),
                new ArrayList<>(s.getCourses().keySet()));
        assertEquals(List.of("CSC108H1 appears 2 times (retake?)"), s.getPlanWarnings());
        assertEquals("", s.getError());
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertSame(s, events.get(0).getNewValue());
    }

    @Test
    void failSetsError() {
        CourseEligibility c = course(MET);
        present(c);

        presenter.prepareFailView("That plan no longer exists.");

        EligibilityState s = viewModel.getState();
        assertEquals("That plan no longer exists.", s.getError());
        assertTrue(s.getCourses().isEmpty());
        assertTrue(s.getPlanWarnings().isEmpty());
        assertNull(s.getPlanId());
        assertEquals(2, events.size());
    }
}
