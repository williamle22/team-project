package use_case.check_graduation;

/** Receives Use Case 10's results. For each check, the interactor calls exactly one of these methods. */
public interface CheckGraduationOutputBoundary {
    void prepareSuccessView(CheckGraduationOutputData data);
    /**
     * Called when the catalog hasn't loaded yet, the plan doesn't exist, or the plan has no degree. The view shows the
     * message to the user.
     */
    void prepareFailView(String message);
}
