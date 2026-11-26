package use_case.add_term_to_plan;

/** Adds the next supported term to a plan. */
public interface AddTermToPlanInputBoundary {
    void execute(AddTermToPlanInputData input);
}
