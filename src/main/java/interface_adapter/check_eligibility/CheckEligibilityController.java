package interface_adapter.check_eligibility;

import use_case.check_eligibility.CheckEligibilityInputBoundary;
import use_case.check_eligibility.CheckEligibilityInputData;

import java.util.UUID;

public final class CheckEligibilityController {
    private final CheckEligibilityInputBoundary interactor;

    public CheckEligibilityController(CheckEligibilityInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(UUID planId) {
        interactor.execute(new CheckEligibilityInputData(planId));
    }
}
