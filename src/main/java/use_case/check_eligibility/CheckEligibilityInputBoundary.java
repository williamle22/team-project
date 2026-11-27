package use_case.check_eligibility;

/** Use Case 8: Check Eligibility. */
public interface CheckEligibilityInputBoundary {
    /** Checks every course in the plan and sends the results to the presenter. */
    void execute(CheckEligibilityInputData input);
}
