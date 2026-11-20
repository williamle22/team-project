package use_case.check_eligibility;

import entity.evaluation.Status;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CourseEligibility {
    private final UUID plannedCourseId;
    private final String code;
    private final String placementLabel;
    private final Status overall;
    private final List<EligibilityLine> sections;
    private final List<String> warnings;

    public CourseEligibility(UUID plannedCourseId, String code, String placementLabel, Status overall,
                             List<EligibilityLine> sections, List<String> warnings) {
        this.plannedCourseId = Objects.requireNonNull(plannedCourseId, "plannedCourseId");
        this.code = Objects.requireNonNull(code, "code");
        this.placementLabel = Objects.requireNonNull(placementLabel, "placementLabel");
        this.overall = Objects.requireNonNull(overall, "overall");
        this.sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
    }

    public UUID getPlannedCourseId() {
        return plannedCourseId;
    }

    public String getCode() {
        return code;
    }

    public String getPlacementLabel() {
        return placementLabel;
    }

    public Status getOverall() {
        return overall;
    }

    public List<EligibilityLine> getSections() {
        return sections;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseEligibility)) {
            return false;
        }
        CourseEligibility that = (CourseEligibility) o;
        return plannedCourseId.equals(that.plannedCourseId)
                && code.equals(that.code)
                && placementLabel.equals(that.placementLabel)
                && overall == that.overall
                && sections.equals(that.sections)
                && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plannedCourseId, code, placementLabel, overall, sections, warnings);
    }

    @Override
    public String toString() {
        return "CourseEligibility{" + code + " in " + placementLabel + ", " + overall + ", id=" + plannedCourseId
                + ", sections=" + sections + ", warnings=" + warnings + "}";
    }
}
