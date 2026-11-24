package use_case.view_course_details;

/** Use Case 3: View Course Details. */
public interface ViewCourseDetailsInputBoundary {
    /** Searches the Course Details sends the results to the presenter. */
    void execute(ViewCourseDetailsInputData input);
}
