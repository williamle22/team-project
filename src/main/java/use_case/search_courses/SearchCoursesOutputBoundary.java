package use_case.search_courses;

/** Receives Use Case 2's results. For each search, the interactor calls exactly one of these methods. */
public interface SearchCoursesOutputBoundary {
    void prepareSuccessView(SearchCoursesOutputData data);
    /** Called when the catalog hasn't loaded yet. The view shows the message to the user. */
    void prepareFailView(String message);
}
