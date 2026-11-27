package use_case.add_term_to_plan;

import entity.plan.Plan;

import java.util.Objects;

/** Contains the updated plan and message after adding a term. */
public final class AddTermToPlanOutputData {
    private final Plan plan;
    private final String message;

    public AddTermToPlanOutputData(Plan plan, String message) {
        this.plan = Objects.requireNonNull(plan, "plan").copy();
        this.message = Objects.requireNonNull(message, "message");
    }

    public Plan getPlan() {
        return plan.copy();
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddTermToPlanOutputData)) {
            return false;
        }
        AddTermToPlanOutputData that = (AddTermToPlanOutputData) o;
        return plan.equals(that.plan) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plan, message);
    }

    @Override
    public String toString() {
        return "AddTermToPlanOutputData{" + plan + ", \"" + message + "\"}";
    }
}
