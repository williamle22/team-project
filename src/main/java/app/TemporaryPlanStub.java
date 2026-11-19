package app;

import entity.plan.Plan;
import entity.term.AcademicTerm;
import use_case.PlanRepository;

import java.time.Year;
import java.util.Objects;

/** Temporary startup plan data until the home pane can load or create a plan. */
public final class TemporaryPlanStub {
    private TemporaryPlanStub() {}

    /** Creates and saves a plan with the current calendar year's Fall and following Winter terms. */
    public static Plan createAndSave(PlanRepository plans) {
        Objects.requireNonNull(plans, "plans");
        int fallYear = Year.now().getValue();
        Plan plan = Plan.create("My Plan");
        plan.addTerm(AcademicTerm.fall(fallYear));
        plan.addTerm(AcademicTerm.winter(fallYear + 1));
        plans.save(plan);
        return plan;
    }
}
