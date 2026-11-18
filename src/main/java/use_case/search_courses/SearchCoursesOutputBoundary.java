package use_case.search_courses;

public interface SearchCoursesOutputBoundary {
    void prepareSuccessView(SearchCoursesOutputData data);
    void prepareFailView(String message);
}
