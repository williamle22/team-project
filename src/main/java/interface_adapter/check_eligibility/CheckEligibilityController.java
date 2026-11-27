package interface_adapter.check_eligibility;

import use_case.check_eligibility.CheckEligibilityInputBoundary;
import use_case.check_eligibility.CheckEligibilityInputData;

import java.util.UUID;

/** Starts Use Case 8. Call it after every change to a plan. The check runs on the calling thread. */
public final class CheckEligibilityController {
    private final CheckEligibilityInputBoundary interactor;

    public CheckEligibilityController(CheckEligibilityInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(UUID planId) {
        interactor.execute(new CheckEligibilityInputData(planId));
    }
}
