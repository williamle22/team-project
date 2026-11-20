package use_case.check_eligibility;

public interface CheckEligibilityOutputBoundary {
    void prepareSuccessView(CheckEligibilityOutputData data);
    void prepareFailView(String message);
}
