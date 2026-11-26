package use_case.add_course_to_plan;

import data_access.catalog.InMemoryCatalogRepository;
import data_access.plan.InMemoryPlanRepository;
import entity.catalog.CatalogVersion;
import entity.plan.Plan;
import entity.term.AcademicTerm;
import entity.term.Placement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PlanRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static entity.catalog.SessionPeriod.SUMMER;
import static entity.catalog.Section.F;
import static entity.catalog.Section.Y;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.CatalogFixtures.course;

class AddCourseToPlanInteractorTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    private PlanRepository plans;
    private RecordingPresenter presenter;
    private AddCourseToPlanInteractor interactor;

    @BeforeEach
    void setUp() {
        plans = new InMemoryPlanRepository();
        presenter = new RecordingPresenter();
        interactor = new AddCourseToPlanInteractor(plans,
                new InMemoryCatalogRepository(List.of(course("CSC148H1"), course("MAT137Y1")), List.of(),
                        new CatalogVersion("2026-2027", Instant.parse("2026-09-01T00:00:00Z"))),
                presenter);
    }

    @Test
    void addsHalfCourseToFallAndSavesPlan() {
        Plan plan = planWith(AcademicTerm.fall(2026));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.fall(2026)));

        assertEquals(List.of("success"), presenter.calls);
        assertEquals(1, plans.findPlan(PLAN_ID).orElseThrow().getCourses().size());
        Placement placement = presenter.data.getPlan().getCourses().get(0).getPlacement();
        assertEquals(Placement.fallWinter(2026, F), placement);
        assertEquals(plan.getId(), presenter.data.getPlan().getId());
    }

    @Test
    void addsFullYearCourseToFallWinterPlacement() {
        planWith(AcademicTerm.fall(2026), AcademicTerm.winter(2027));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "MAT137Y1", AcademicTerm.winter(2027)));

        Placement placement = plans.findPlan(PLAN_ID).orElseThrow().getCourses().get(0).getPlacement();
        assertEquals(Placement.fallWinter(2026, Y), placement);
    }

    @Test
    void addsFullYearCourseStartingInFall() {
        planWith(AcademicTerm.fall(2026), AcademicTerm.winter(2027));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "MAT137Y1", AcademicTerm.fall(2026)));

        Placement placement = plans.findPlan(PLAN_ID).orElseThrow().getCourses().get(0).getPlacement();
        assertEquals(Placement.fallWinter(2026, Y), placement);
    }

    @Test
    void addsSummerCourseInSectionF() {
        planWith(AcademicTerm.summer(2027));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "MAT137Y1", AcademicTerm.summer(2027)));

        Placement placement = plans.findPlan(PLAN_ID).orElseThrow().getCourses().get(0).getPlacement();
        assertEquals(Placement.summer(2027, F), placement);
        assertEquals(SUMMER, placement.getPeriod());
    }

    @Test
    void missingPlanReportsFailure() {
        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.fall(2026)));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("The academic plan couldn't be found.", presenter.failMessage);
    }

    @Test
    void missingCourseReportsFailure() {
        planWith(AcademicTerm.fall(2026));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "BIO120H1", AcademicTerm.fall(2026)));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Course BIO120H1 isn't available in the loaded catalog.", presenter.failMessage);
        assertEquals(0, plans.findPlan(PLAN_ID).orElseThrow().getCourses().size());
    }

    @Test
    void invalidPlacementReportsFailureAndDoesNotSaveCourse() {
        planWith(AcademicTerm.fall(2026));

        interactor.execute(new AddCourseToPlanInputData(PLAN_ID, "CSC148H1", AcademicTerm.winter(2027)));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Failed to add course CSC148H1 to plan.", presenter.failMessage);
        assertEquals(0, plans.findPlan(PLAN_ID).orElseThrow().getCourses().size());
    }

    private Plan planWith(AcademicTerm... terms) {
        Plan plan = Plan.restore(PLAN_ID, "Test plan", null, List.of(), List.of(), List.of(), List.of());
        for (AcademicTerm term : terms) {
            plan.addTerm(term);
        }
        plans.save(plan);
        return plan;
    }

    private static final class RecordingPresenter implements AddCourseToPlanOutputBoundary {
        private final List<String> calls = new ArrayList<>();
        private AddCourseToPlanOutputData data;
        private String failMessage;

        @Override
        public void prepareSuccessView(AddCourseToPlanOutputData data) {
            calls.add("success");
            this.data = data;
        }

        @Override
        public void prepareFailView(String message) {
            calls.add("fail");
            failMessage = message;
        }
    }
}
