package entity.evaluation;

import java.util.List;
import java.util.Objects;

public final class EligibilityResult {
    private final String courseCode;
    private final EvaluationNode prerequisites;
    private final EvaluationNode corequisites;
    private final ExclusionOutcome exclusions;
    private final OfferingStatus offering;
    private final List<String> warnings;

    private EligibilityResult(String courseCode, EvaluationNode prerequisites, EvaluationNode corequisites,
                              ExclusionOutcome exclusions, OfferingStatus offering, List<String> warnings) {
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.prerequisites = Objects.requireNonNull(prerequisites, "prerequisites");
        this.corequisites = Objects.requireNonNull(corequisites, "corequisites");
        this.exclusions = Objects.requireNonNull(exclusions, "exclusions");
        this.offering = Objects.requireNonNull(offering, "offering");
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
    }

    public static EligibilityResult of(String courseCode, EvaluationNode prerequisites, EvaluationNode corequisites,
                                       ExclusionOutcome exclusions, OfferingStatus offering, List<String> warnings) {
        return new EligibilityResult(courseCode, prerequisites, corequisites, exclusions, offering, warnings);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public EvaluationNode getPrerequisites() {
        return prerequisites;
    }

    public EvaluationNode getCorequisites() {
        return corequisites;
    }

    public ExclusionOutcome getExclusions() {
        return exclusions;
    }

    public OfferingStatus getOffering() {
        return offering;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Status getOverall() {
        Status offered = offering == OfferingStatus.NOT_OFFERED ? Status.NOT_MET : Status.MET;

        return Status.allOf(List.of(prerequisites.getStatus(), corequisites.getStatus(),
                exclusions.getResult().toStatus(), offered));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EligibilityResult)) {
            return false;
        }
        EligibilityResult that = (EligibilityResult) o;
        return courseCode.equals(that.courseCode)
                && prerequisites.equals(that.prerequisites)
                && corequisites.equals(that.corequisites)
                && exclusions.equals(that.exclusions)
                && offering == that.offering
                && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode, prerequisites, corequisites, exclusions, offering, warnings);
    }

    @Override
    public String toString() {
        return "EligibilityResult{" + courseCode + ", " + getOverall() + ", prerequisites=" + prerequisites
                + ", corequisites=" + corequisites + ", exclusions=" + exclusions + ", offering=" + offering
                + ", warnings=" + warnings + "}";
    }
}
