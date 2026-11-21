package use_case.check_eligibility;

/** Receives Use Case 8's results. For each check, the interactor calls exactly one of these methods. */
public interface CheckEligibilityOutputBoundary {
    void prepareSuccessView(CheckEligibilityOutputData data);
    /** Called when the catalog hasn't loaded yet or the plan doesn't exist. The view shows the message to the user. */
    void prepareFailView(String message);
}
