package use_case.add_course_to_plan;

import entity.term.AcademicTerm;

import java.util.Objects;
import java.util.UUID;

/** The plan, course, and term for one add-course request. */
public final class AddCourseToPlanInputData {
    private final UUID planId;
    private final String courseCode;
    private final AcademicTerm targetTerm;

    public AddCourseToPlanInputData(UUID planId, String courseCode, AcademicTerm targetTerm) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.targetTerm = Objects.requireNonNull(targetTerm, "targetTerm");
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    /** Returns the requested term, or {@code null} when no term was supplied. */
    public AcademicTerm getTargetTerm() {
        return targetTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddCourseToPlanInputData)) {
            return false;
        }
        AddCourseToPlanInputData that = (AddCourseToPlanInputData) o;
        return planId.equals(that.planId) && courseCode.equals(that.courseCode)
                && targetTerm.equals(that.targetTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, courseCode, targetTerm);
    }

    @Override
    public String toString() {
        return "AddCourseToPlanInputData{" + planId + ", " + courseCode + ", " + targetTerm + "}";
    }
}
