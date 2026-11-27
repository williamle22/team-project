package use_case.add_course_to_plan;

public interface AddCourseToPlanOutputBoundary {
    void prepareSuccessView(AddCourseToPlanOutputData data);

    void prepareFailView(String message);
}
