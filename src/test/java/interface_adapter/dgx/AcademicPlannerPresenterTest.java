package interface_adapter.dgx;

import entity.plan.Plan;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.add_course_to_plan.AddCourseToPlanOutputData;
import use_case.add_term_to_plan.AddTermToPlanOutputData;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static entity.catalog.Section.F;
import static entity.catalog.Section.S;
import static entity.catalog.Section.Y;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AcademicPlannerPresenterTest {
    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private AcademicPlannerViewModel viewModel;
    private AcademicPlannerPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new AcademicPlannerViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new AcademicPlannerPresenter(viewModel);
    }

    @Test
    void mapsPlanTermsAndCoursesInReverseChronologicalOrder() {
        Plan plan = planWithTerms();
        plan.addCourse("CSC148H1", Placement.fallWinter(2026, F));
        plan.addCourse("MAT137Y1", Placement.fallWinter(2026, Y));
        plan.addCourse("CSC207H1", Placement.fallWinter(2026, S));
        plan.addCourse("BIO130H1", Placement.summer(2027, F));

        presenter.prepareSuccessView(new AddCourseToPlanOutputData(plan, "Added course."));

        List<AcademicPlannerState.TermRow> terms = viewModel.getState().getTerms();
        assertEquals(List.of("Summer 2027", "Winter 2027", "Fall 2026"),
                terms.stream().map(AcademicPlannerState.TermRow::getLabel).collect(Collectors.toList()));
        assertEquals(List.of("BIO130H1"), courseCodes(terms.get(0)));
        assertEquals(List.of("MAT137Y1", "CSC207H1"), courseCodes(terms.get(1)));
        assertEquals(List.of("CSC148H1", "MAT137Y1"), courseCodes(terms.get(2)));
        assertEquals("Added course.", viewModel.getState().getMessage());
        assertStateChange();
    }

    @Test
    void addTermSuccessUsesUpdatedPlan() {
        Plan plan = planWithTerms();

        presenter.prepareSuccessView(new AddTermToPlanOutputData(plan, "Added Summer 2027."));

        assertEquals(List.of("Summer 2027", "Winter 2027", "Fall 2026"),
                viewModel.getState().getTerms().stream().map(AcademicPlannerState.TermRow::getLabel)
                        .collect(Collectors.toList()));
        assertEquals("Added Summer 2027.", viewModel.getState().getMessage());
        assertStateChange();
    }

    @Test
    void failurePreservesTermsAndUpdatesMessage() {
        presenter.presentPlan(planWithTerms(), "");
        List<AcademicPlannerState.TermRow> previousTerms = viewModel.getState().getTerms();
        events.clear();

        presenter.prepareFailView("Course isn't available.");

        assertSame(previousTerms, viewModel.getState().getTerms());
        assertEquals("Course isn't available.", viewModel.getState().getMessage());
        assertStateChange();
    }

    private void assertStateChange() {
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertSame(viewModel.getState(), events.get(0).getNewValue());
    }

    private static List<String> courseCodes(AcademicPlannerState.TermRow term) {
        return term.getCourses().stream().map(AcademicPlannerState.CourseRow::getCode).collect(Collectors.toList());
    }

    private static Plan planWithTerms() {
        Plan plan = Plan.create("Test plan");
        plan.addTerm(AcademicTerm.fall(2026));
        plan.addTerm(AcademicTerm.winter(2027));
        plan.addTerm(AcademicTerm.summer(2027));
        return plan;
    }
}
