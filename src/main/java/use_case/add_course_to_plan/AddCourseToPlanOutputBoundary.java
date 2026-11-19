package use_case.add_course_to_plan;

import entity.plan.Plan;

public interface AddCourseToPlanOutputBoundary {
    void prepareSuccessView(Plan plan, String message);

    void prepareFailView(String message);
}
