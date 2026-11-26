package use_case.add_term_to_plan;

import data_access.plan.InMemoryPlanRepository;
import entity.plan.Plan;
import entity.term.AcademicTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PlanRepository;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddTermToPlanInteractorTest {
    private static final UUID PLAN_ID = UUID.randomUUID();

    private PlanRepository plans;
    private RecordingPresenter presenter;
    private AddTermToPlanInteractor interactor;

    @BeforeEach
    void setUp() {
        plans = new InMemoryPlanRepository();
        presenter = new RecordingPresenter();
        interactor = new AddTermToPlanInteractor(plans, presenter);
    }

    @Test
    void addsFallOfCurrentYearToEmptyPlan() {
        savePlan();

        interactor.execute(new AddTermToPlanInputData(PLAN_ID));

        assertEquals(List.of("success"), presenter.calls);
        assertEquals(List.of(AcademicTerm.fall(Year.now().getValue())), storedPlan().getTerms());
        assertEquals("Added Fall " + Year.now().getValue() + " to your plan.", presenter.data.getMessage());
    }

    @Test
    void addsSummerAfterWinter() {
        savePlan(AcademicTerm.winter(2027));

        interactor.execute(new AddTermToPlanInputData(PLAN_ID));

        assertEquals(List.of(AcademicTerm.winter(2027), AcademicTerm.summer(2027)), storedPlan().getTerms());
        assertEquals("Added Summer 2027 to your plan.", presenter.data.getMessage());
    }

    @Test
    void addsFallAfterSummer() {
        savePlan(AcademicTerm.summer(2027));

        interactor.execute(new AddTermToPlanInputData(PLAN_ID));

        assertEquals(List.of(AcademicTerm.summer(2027), AcademicTerm.fall(2027)), storedPlan().getTerms());
        assertEquals("Added Fall 2027 to your plan.", presenter.data.getMessage());
    }

    @Test
    void missingPlanReportsFailure() {
        interactor.execute(new AddTermToPlanInputData(PLAN_ID));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("The academic plan couldn't be found.", presenter.failMessage);
    }

    @Test
    void duplicateNextTermReportsFailureAndDoesNotSave() {
        Plan plan = savePlan(AcademicTerm.winter(Integer.MIN_VALUE), AcademicTerm.fall(Integer.MAX_VALUE));

        interactor.execute(new AddTermToPlanInputData(PLAN_ID));

        assertEquals(List.of("fail"), presenter.calls);
        assertEquals("Winter " + Integer.MIN_VALUE + " is already in this plan", presenter.failMessage);
        assertEquals(plan.getTerms(), storedPlan().getTerms());
    }

    @Test
    void rejectsNullDependenciesAndInput() {
        assertThrows(NullPointerException.class, () -> new AddTermToPlanInteractor(null, presenter));
        assertThrows(NullPointerException.class, () -> new AddTermToPlanInteractor(plans, null));
        assertThrows(NullPointerException.class, () -> interactor.execute(null));
    }

    private Plan savePlan(AcademicTerm... terms) {
        Plan plan = Plan.restore(PLAN_ID, "Test plan", null, List.of(), List.of(), List.of(), List.of());
        for (AcademicTerm term : terms) {
            plan.addTerm(term);
        }
        plans.save(plan);
        return plan;
    }

    private Plan storedPlan() {
        return plans.findPlan(PLAN_ID).orElseThrow();
    }

    private static final class RecordingPresenter implements AddTermToPlanOutputBoundary {
        private final List<String> calls = new ArrayList<>();
        private AddTermToPlanOutputData data;
        private String failMessage;

        @Override
        public void prepareSuccessView(AddTermToPlanOutputData data) {
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
