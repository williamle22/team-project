package use_case.add_term_to_plan;

import java.util.Objects;
import java.util.UUID;

/** Identifies the plan whose next term should be added. */
public final class AddTermToPlanInputData {
    private final UUID planId;

    public AddTermToPlanInputData(UUID planId) {
        this.planId = Objects.requireNonNull(planId, "planId");
    }

    public UUID getPlanId() {
        return planId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddTermToPlanInputData)) {
            return false;
        }
        AddTermToPlanInputData that = (AddTermToPlanInputData) o;
        return planId.equals(that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId);
    }

    @Override
    public String toString() {
        return "AddTermToPlanInputData{" + planId + "}";
    }
}
