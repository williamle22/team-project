package entity.grades;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A student's grade record for one course: an optional final grade from 0-100 and the assessments that lead to it.
 * Assessment weights are percent of the final grade. The {@code with} methods return modified copies.
 *
 * <p>Only {@link #getFinalGrade()} can feed requirement evaluation. A predicted grade, such as 82%, must never satisfy
 * a minimum-grade prerequisite, such as 80%. For this reason, no method returns "the grade" without naming its kind.
 */
public final class CourseGrade {
    private final Integer finalGrade;
    private final List<Assessment> assessments;

    /** @throws IllegalArgumentException if {@code finalGrade} isn't 0-100 */
    public CourseGrade(Integer finalGrade, List<Assessment> assessments) {
        if (finalGrade != null && (finalGrade < 0 || finalGrade > 100)) {
            throw new IllegalArgumentException("finalGrade must be 0-100, was " + finalGrade);
        }
        this.finalGrade = finalGrade;
        this.assessments = List.copyOf(Objects.requireNonNull(assessments, "assessments"));
    }

    public static CourseGrade empty() {
        return new CourseGrade(null, List.of());
    }

    public Optional<Integer> getFinalGrade() {
        return Optional.ofNullable(finalGrade);
    }

    public boolean hasFinalGrade() {
        return finalGrade != null;
    }

    public List<Assessment> getAssessments() {
        return assessments;
    }

    public CourseGrade withFinalGrade(Integer finalGrade) {
        return new CourseGrade(finalGrade, assessments);
    }

    public CourseGrade withAssessments(List<Assessment> assessments) {
        return new CourseGrade(finalGrade, assessments);
    }

    public CourseGrade withAssessmentAdded(Assessment a) {
        List<Assessment> list = new ArrayList<>(assessments);
        list.add(a);
        return withAssessments(list);
    }

    public CourseGrade withAssessmentReplaced(int index, Assessment a) {
        List<Assessment> list = new ArrayList<>(assessments);
        list.set(index, a);
        return withAssessments(list);
    }

    public CourseGrade withAssessmentRemoved(int index) {
        List<Assessment> list = new ArrayList<>(assessments);
        list.remove(index);
        return withAssessments(list);
    }

    public double totalWeight() {
        return assessments.stream().mapToDouble(Assessment::getWeight).sum();
    }

    public double gradedWeight() {
        return assessments.stream().filter(Assessment::isGraded).mapToDouble(Assessment::getWeight).sum();
    }

    /** Returns the total weight of ungraded assessments. */
    public double remainingWeight() {
        return assessments.stream().filter(a -> !a.isGraded()).mapToDouble(Assessment::getWeight).sum();
    }

    private double earned() {
        return assessments.stream().filter(Assessment::isGraded)
                .mapToDouble(a -> a.getScore().get() * a.getWeight()).sum();
    }

    /** Returns the weighted average of graded assessments as a percent, or empty if nothing is graded. */
    public OptionalDouble currentAverage() {
        double graded = gradedWeight();
        return graded == 0 ? OptionalDouble.empty() : OptionalDouble.of(earned() / graded);
    }

    /** Returns the final grade if every ungraded assessment scores 0, or empty if there are no assessments. */
    public OptionalDouble guaranteedMinimum() {
        double total = totalWeight();
        return total == 0 ? OptionalDouble.empty() : OptionalDouble.of(earned() / total);
    }

    /**
     * Returns the average needed on all ungraded assessments to finish at {@code target} percent, or empty if nothing
     * is ungraded. A result above 100 means the target is out of reach. A result below 0 means the target is already
     * secured.
     *
     * @throws IllegalArgumentException if {@code target} isn't finite
     */
    public OptionalDouble neededOnRemaining(double target) {
        if (!Double.isFinite(target)) {
            throw new IllegalArgumentException("target must be a finite number, was " + target);
        }
        double remaining = remainingWeight();
        if (remaining == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((target * totalWeight() - earned()) / remaining);
    }

    /**
     * Returns a message if assessment weights don't total 100, or empty if they do or there are no assessments. The
     * message is a warning, not an error, because some syllabi drop the lowest marks.
     */
    public Optional<String> weightWarning() {
        double total = totalWeight();
        if (assessments.isEmpty() || Math.abs(total - 100) <= 1e-9) {
            return Optional.empty();
        }
        return Optional.of(String.format("Assessment weights total %.2f%%, not 100%%.", total));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseGrade)) {
            return false;
        }
        CourseGrade that = (CourseGrade) o;
        return Objects.equals(finalGrade, that.finalGrade) && assessments.equals(that.assessments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(finalGrade, assessments);
    }

    @Override
    public String toString() {
        return "CourseGrade{" + (finalGrade == null ? "" : "final " + finalGrade + ", ") + assessments + "}";
    }
}
