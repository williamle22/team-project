package use_case.check_eligibility;

import java.util.Objects;
import java.util.UUID;

public final class CheckEligibilityInputData {
    private final UUID planId;

    public CheckEligibilityInputData(UUID planId) {
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
        if (!(o instanceof CheckEligibilityInputData)) {
            return false;
        }
        CheckEligibilityInputData that = (CheckEligibilityInputData) o;
        return planId.equals(that.planId);
    }

    @Override
    public int hashCode() {
        return planId.hashCode();
    }

    @Override
    public String toString() {
        return "CheckEligibilityInputData{" + planId + "}";
    }
}
