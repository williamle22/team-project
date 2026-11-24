package use_case.view_course_details;

/** Receives Use Case 3 results. For each search for course details, the interactor uses one of these methods*/
public interface ViewCourseDetailsOutputBoundary {
    void prepareSuccessView(ViewCourseDetailsOutputBoundary data);
    /** Called when course details hasn't loaded. The view shows the message to the user*/
    void prepareFailView(String message);
}
