package use_case.add_course_to_plan;

/** Adds a catalog course to a plan at the requested academic term. */
public interface AddCourseToPlanInputBoundary {
    void execute(AddCourseToPlanInputData input);
}
