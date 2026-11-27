package interface_adapter.check_eligibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * What the eligibility views show for one plan. {@code courses} is keyed by planned course ID. If {@code error} isn't
 * empty, the check failed and the other fields are empty.
 */
public final class EligibilityState {
    private UUID planId;
    private Map<UUID, CourseEligibilityView> courses = Map.of();
    private List<String> planWarnings = List.of();
    private String error = "";

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public Map<UUID, CourseEligibilityView> getCourses() {
        return courses;
    }

    public void setCourses(Map<UUID, CourseEligibilityView> courses) {
        this.courses = Collections.unmodifiableMap(new LinkedHashMap<>(courses));
    }

    public List<String> getPlanWarnings() {
        return planWarnings;
    }

    public void setPlanWarnings(List<String> planWarnings) {
        this.planWarnings = List.copyOf(planWarnings);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EligibilityState)) {
            return false;
        }
        EligibilityState that = (EligibilityState) o;
        return Objects.equals(planId, that.planId) && Objects.equals(courses, that.courses)
                && Objects.equals(planWarnings, that.planWarnings) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, courses, planWarnings, error);
    }

    @Override
    public String toString() {
        return "EligibilityState{" + planId + ", " + courses.size() + " courses, planWarnings=" + planWarnings
                + ", error=" + error + "}";
    }
}
