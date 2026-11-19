package interface_adapter.dgx;

import java.util.List;

/** Search results displayed by the course panel. These are sample results until catalog search is connected. */
public class CourseSearchState {
    private final List<String> courseCodes;

    public CourseSearchState() {
        this(List.of("CSC108H1", "CSC148H1", "MAT137Y1", "CSC110Y1")); // TODO remove: Temporary testing values
    }

    public CourseSearchState(List<String> courseCodes) {
        this.courseCodes = List.copyOf(courseCodes);
    }

    public List<String> getCourseCodes() {
        return courseCodes;
    }
}
