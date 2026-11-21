package interface_adapter.check_eligibility;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One course's eligibility, formatted for display. {@code status} is {@code "met"}, {@code "not met"}, or {@code
 * "review"}.
 */
public final class CourseEligibilityView {
    private final UUID plannedCourseId;
    private final String code;
    private final String status;
    private final String summary;
    private final List<EligibilityLineView> lines;
    private final List<String> warnings;

    public CourseEligibilityView(UUID plannedCourseId, String code, String status, String summary,
                                 List<EligibilityLineView> lines, List<String> warnings) {
        this.plannedCourseId = Objects.requireNonNull(plannedCourseId, "plannedCourseId");
        this.code = Objects.requireNonNull(code, "code");
        this.status = Objects.requireNonNull(status, "status");
        this.summary = Objects.requireNonNull(summary, "summary");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
    }

    public UUID getPlannedCourseId() {
        return plannedCourseId;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public List<EligibilityLineView> getLines() {
        return lines;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseEligibilityView)) {
            return false;
        }
        CourseEligibilityView that = (CourseEligibilityView) o;
        return plannedCourseId.equals(that.plannedCourseId)
                && code.equals(that.code)
                && status.equals(that.status)
                && summary.equals(that.summary)
                && lines.equals(that.lines)
                && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plannedCourseId, code, status, summary, lines, warnings);
    }

    @Override
    public String toString() {
        return "CourseEligibilityView{" + code + ", " + status + ", \"" + summary + "\", id=" + plannedCourseId
                + ", lines=" + lines + ", warnings=" + warnings + "}";
    }
}
