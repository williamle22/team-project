package use_case.search_programs;

/** Receives the program search's results. For each search, the interactor calls exactly one of these methods. */
public interface SearchProgramsOutputBoundary {
    void prepareSuccessView(SearchProgramsOutputData data);
    /** Called when the catalog hasn't loaded yet. The view shows the message to the user. */
    void prepareFailView(String message);
}
