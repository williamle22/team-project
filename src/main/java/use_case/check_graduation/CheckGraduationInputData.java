package use_case.check_graduation;

import java.util.Objects;
import java.util.UUID;

/**
 * The plan to check. If {@code projected} is true, every course in the plan counts except failed ones. Otherwise, only
 * courses with a passing final grade count.
 */
public final class CheckGraduationInputData {
    private final UUID planId;
    private final boolean projected;

    public CheckGraduationInputData(UUID planId, boolean projected) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.projected = projected;
    }

    public UUID getPlanId() {
        return planId;
    }

    public boolean isProjected() {
        return projected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CheckGraduationInputData)) {
            return false;
        }
        CheckGraduationInputData that = (CheckGraduationInputData) o;
        return planId.equals(that.planId) && projected == that.projected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, projected);
    }

    @Override
    public String toString() {
        return "CheckGraduationInputData{" + planId + (projected ? ", whole plan" : ", completed only") + "}";
    }
}
