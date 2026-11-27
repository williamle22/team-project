package use_case.add_term_to_plan;

/** Presents the result of adding a term to a plan. */
public interface AddTermToPlanOutputBoundary {
    void prepareSuccessView(AddTermToPlanOutputData data);

    void prepareFailView(String message);
}
