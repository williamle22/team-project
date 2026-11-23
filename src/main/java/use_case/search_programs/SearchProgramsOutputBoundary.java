package use_case.search_programs;

public interface SearchProgramsOutputBoundary {
    void prepareSuccessView(SearchProgramsOutputData data);
    void prepareFailView(String message);
}
