package entity.grades;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public final class CourseGrade {
    private final Integer finalGrade;
    private final List<Assessment> assessments;

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

    public double remainingWeight() {
        return assessments.stream().filter(a -> !a.isGraded()).mapToDouble(Assessment::getWeight).sum();
    }

    private double earned() {
        return assessments.stream().filter(Assessment::isGraded)
                .mapToDouble(a -> a.getScore().get() * a.getWeight()).sum();
    }

    public OptionalDouble currentAverage() {
        double graded = gradedWeight();
        return graded == 0 ? OptionalDouble.empty() : OptionalDouble.of(earned() / graded);
    }

    public OptionalDouble guaranteedMinimum() {
        double total = totalWeight();
        return total == 0 ? OptionalDouble.empty() : OptionalDouble.of(earned() / total);
    }

    public OptionalDouble neededOnRemaining(double target) {
        double remaining = remainingWeight();
        if (remaining == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((target * totalWeight() - earned()) / remaining);
    }

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
