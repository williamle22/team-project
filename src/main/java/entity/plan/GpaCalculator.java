package entity.plan;

import entity.grades.LetterGrade;
import entity.term.AcademicTerm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Calculates credit-weighted GPAs for a plan: the sum of {@code LetterGrade} points times weight, divided by the sum of
 * weights. It excludes CR/NCR courses, transfer credits, and attempts designated Extra. A failing grade counts as 0.0.
 * Each method returns empty when no credits count.
 *
 * <p>Retakes follow "Repeating Failed Courses", "Repeating Passed Courses", and "Extra" courses in the
 * <a href="https://artsci.calendar.utoronto.ca/course-enrolment">Arts and Science course enrolment rules</a>. Every
 * attempt before the first pass counts. From the first pass on, a repeat with a higher mark replaces the earlier
 * attempt (Second Attempt for Credit). Up to 2.0 credits of replacements apply across the record, in chronological
 * order. Any other repeat is Extra. The CR/NCR and transfer credit exclusions come from "Grade Point Average" in the
 * <a href="https://artsci.calendar.utoronto.ca/academic-record">Arts and Science academic record rules</a>.
 */
public final class GpaCalculator {
    private static final double SECOND_ATTEMPT_CAP = 2.0;

    private final CreditWeights weights;

    public GpaCalculator(CreditWeights weights) {
        this.weights = Objects.requireNonNull(weights, "weights");
    }

    /** Returns the CGPA over all courses with a final grade. */
    public OptionalDouble cgpa(Plan plan) {
        return gpa(plan, GpaCalculator::finalGrade, c -> true);
    }

    /** Returns the CGPA over courses that end before {@code term}. */
    public OptionalDouble cgpaBefore(Plan plan, AcademicTerm term) {
        return gpa(plan, GpaCalculator::finalGrade, c -> c.getPlacement().endTerm().isBefore(term));
    }

    /**
     * Returns the sessional GPA over courses that end in {@code term}. A Fall-Winter Y course counts toward Winter,
     * the term in which the student gets its mark.
     */
    public OptionalDouble sgpa(Plan plan, AcademicTerm term) {
        return gpa(plan, GpaCalculator::finalGrade, c -> c.getPlacement().endTerm().equals(term));
    }

    /**
     * Returns the CGPA, using {@code currentAverage()} rounded and capped at 100 for courses without a final grade.
     * Always label this value "projected" in the UI.
     */
    public OptionalDouble projectedCgpa(Plan plan) {
        return gpa(plan, GpaCalculator::projectedGrade, c -> true);
    }

    private OptionalDouble gpa(Plan plan, Function<PlannedCourse, Optional<Integer>> grade,
                               Predicate<PlannedCourse> include) {
        List<PlannedCourse> graded = plan.getCourses().stream()
                .filter(c -> !c.isCrNcr() && grade.apply(c).isPresent())
                .collect(Collectors.toList());
        Set<UUID> extras = extraAttempts(graded, grade, weights);
        double points = 0;
        double credits = 0;
        for (PlannedCourse c : graded) {
            if (include.test(c) && !extras.contains(c.getId())) {
                double weight = weights.weightOf(c.getCode());
                points += LetterGrade.fromPercent(grade.apply(c).get()).getPoints() * weight;
                credits += weight;
            }
        }
        return credits == 0 ? OptionalDouble.empty() : OptionalDouble.of(points / credits);
    }

    /**
     * Returns the ids of attempts designated Extra under the retake rules. {@code grade} supplies each attempt's mark.
     */
    static Set<UUID> extraAttempts(List<PlannedCourse> attempts, Function<PlannedCourse, Optional<Integer>> grade,
                                   CreditWeights weights) {
        List<PlannedCourse> inOrder = new ArrayList<>(attempts);
        inOrder.sort(Comparator.comparing(c -> c.getPlacement().endTerm()));
        Set<UUID> extras = new HashSet<>();
        Map<String, PlannedCourse> counted = new HashMap<>();
        double secondAttemptCredits = 0;
        for (PlannedCourse c : inOrder) {
            int mark = grade.apply(c).get();
            PlannedCourse current = counted.get(c.getCode());
            if (current == null) {
                if (LetterGrade.fromPercent(mark).isPassing()) {
                    counted.put(c.getCode(), c);
                }
                continue;
            }
            double weight = weights.weightOf(c.getCode());
            if (mark > grade.apply(current).get() && secondAttemptCredits + weight <= SECOND_ATTEMPT_CAP) {
                extras.add(current.getId());
                counted.put(c.getCode(), c);
                secondAttemptCredits += weight;
            } else {
                extras.add(c.getId());
            }
        }
        return extras;
    }

    static Optional<Integer> finalGrade(PlannedCourse c) {
        return c.getGrade().getFinalGrade();
    }

    private static Optional<Integer> projectedGrade(PlannedCourse c) {
        if (c.getGrade().hasFinalGrade()) {
            return c.getGrade().getFinalGrade();
        }
        OptionalDouble average = c.getGrade().currentAverage();
        if (average.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((int) Math.min(100, Math.round(average.getAsDouble())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GpaCalculator)) {
            return false;
        }
        return weights.equals(((GpaCalculator) o).weights);
    }

    @Override
    public int hashCode() {
        return weights.hashCode();
    }

    @Override
    public String toString() {
        return "GpaCalculator{" + weights + "}";
    }
}
