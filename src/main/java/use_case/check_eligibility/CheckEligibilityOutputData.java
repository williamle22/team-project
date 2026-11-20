package use_case.check_eligibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class CheckEligibilityOutputData {
    private final UUID planId;
    private final Map<UUID, CourseEligibility> courses;
    private final List<String> planWarnings;

    public CheckEligibilityOutputData(UUID planId, Map<UUID, CourseEligibility> courses, List<String> planWarnings) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.courses = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(courses, "courses")));
        this.planWarnings = List.copyOf(Objects.requireNonNull(planWarnings, "planWarnings"));
    }

    public UUID getPlanId() {
        return planId;
    }

    public Map<UUID, CourseEligibility> getCourses() {
        return courses;
    }

    public List<String> getPlanWarnings() {
        return planWarnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CheckEligibilityOutputData)) {
            return false;
        }
        CheckEligibilityOutputData that = (CheckEligibilityOutputData) o;
        return planId.equals(that.planId) && courses.equals(that.courses) && planWarnings.equals(that.planWarnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, courses, planWarnings);
    }

    @Override
    public String toString() {
        return "CheckEligibilityOutputData{" + planId + ", courses=" + courses.values() + ", planWarnings="
                + planWarnings + "}";
    }
}
