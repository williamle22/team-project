package interface_adapter.dgx;

import use_case.add_term_to_plan.AddTermToPlanInputBoundary;
import use_case.add_term_to_plan.AddTermToPlanInputData;

import java.util.Objects;
import java.util.UUID;

/** Sends an add-term request for the bound plan. */
public final class AddTermToPlanController {
    private final UUID planId;
    private final AddTermToPlanInputBoundary interactor;

    public AddTermToPlanController(UUID planId, AddTermToPlanInputBoundary interactor) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.interactor = Objects.requireNonNull(interactor, "interactor");
    }

    public void execute() {
        interactor.execute(new AddTermToPlanInputData(planId));
    }
}
