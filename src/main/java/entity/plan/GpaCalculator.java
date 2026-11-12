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

public final class GpaCalculator {
    private static final double SECOND_ATTEMPT_CAP = 2.0;

    private final CreditWeights weights;

    public GpaCalculator(CreditWeights weights) {
        this.weights = Objects.requireNonNull(weights, "weights");
    }

    public OptionalDouble cgpa(Plan plan) {
        return gpa(plan, GpaCalculator::finalGrade, c -> true);
    }

    public OptionalDouble cgpaBefore(Plan plan, AcademicTerm term) {
        return gpa(plan, GpaCalculator::finalGrade, c -> c.getPlacement().endTerm().isBefore(term));
    }

    public OptionalDouble sgpa(Plan plan, AcademicTerm term) {
        return gpa(plan, GpaCalculator::finalGrade, c -> c.getPlacement().endTerm().equals(term));
    }

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
