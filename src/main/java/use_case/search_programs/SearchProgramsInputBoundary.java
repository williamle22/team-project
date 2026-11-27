package use_case.search_programs;

/** Use Case 2's program search. */
public interface SearchProgramsInputBoundary {
    /** Searches the catalog's programs and sends the results to the presenter. */
    void execute(SearchProgramsInputData input);
}
