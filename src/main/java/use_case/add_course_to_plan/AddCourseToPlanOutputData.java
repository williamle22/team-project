package use_case.add_course_to_plan;

import entity.plan.Plan;

import java.util.Objects;

/** The outcome of adding a course, including the updated plan on success. */
public final class AddCourseToPlanOutputData {
    private final Plan plan;
    private final String message;

    public AddCourseToPlanOutputData(Plan plan, String message) {
        this.message = Objects.requireNonNull(message, "message");
        this.plan = plan == null ? null : plan.copy();
    }

    /** Returns an independent updated plan on success, or {@code null} on failure. */
    public Plan getPlan() {
        return plan == null ? null : plan.copy();
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddCourseToPlanOutputData)) {
            return false;
        }
        AddCourseToPlanOutputData that = (AddCourseToPlanOutputData) o;
        return Objects.equals(plan, that.plan) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plan, message);
    }

    @Override
    public String toString() {
        return "AddCourseToPlanOutputData{" + plan + ", \"" + message + "\"}";
    }
}
