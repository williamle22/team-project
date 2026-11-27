package use_case.search_courses;

/** Use Case 2's course search. */
public interface SearchCoursesInputBoundary {
    /** Searches the catalog and sends the results to the presenter. */
    void execute(SearchCoursesInputData input);
}
