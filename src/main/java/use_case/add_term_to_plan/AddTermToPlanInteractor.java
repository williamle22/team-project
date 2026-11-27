package use_case.add_term_to_plan;

import entity.plan.Plan;
import entity.term.AcademicTerm;
import use_case.PlanRepository;

import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Adds and saves the next academic term in a plan. */
public final class AddTermToPlanInteractor implements AddTermToPlanInputBoundary {
    private final PlanRepository plans;
    private final AddTermToPlanOutputBoundary presenter;

    public AddTermToPlanInteractor(PlanRepository plans, AddTermToPlanOutputBoundary presenter) {
        this.plans = Objects.requireNonNull(plans, "plans");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
    }

    @Override
    public void execute(AddTermToPlanInputData input) {
        Objects.requireNonNull(input, "input");
        Optional<Plan> storedPlan = plans.findPlan(input.getPlanId());
        if (storedPlan.isEmpty()) {
            presenter.prepareFailView("The academic plan couldn't be found.");
            return;
        }

        Plan plan = storedPlan.get();
        AcademicTerm term = nextTerm(plan.getTerms());
        try {
            plan.addTerm(term);
        } catch (IllegalArgumentException e) {
            presenter.prepareFailView(e.getMessage());
            return;
        }

        plans.save(plan);
        presenter.prepareSuccessView(new AddTermToPlanOutputData(plan, "Added " + term + " to your plan."));
    }

    private static AcademicTerm nextTerm(List<AcademicTerm> terms) {
        if (terms.isEmpty()) {
            return AcademicTerm.fall(Year.now().getValue());
        }

        return terms.get(terms.size() - 1).next();
    }
}
