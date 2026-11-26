package entity.evaluation;

import entity.catalog.ProgramType;
import entity.degree.Degree;
import entity.degree.ProgramCombinationRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks a degree's completion rows, and whether the student's completed programs form one of its program combinations.
 * A completed program the student isn't enrolled in gives {@code REVIEW}, because the student still needs to be
 * admitted.
 */
public final class DegreeEvaluator {
    private static final double DIFFERENT_CREDITS = 12.0;

    private final CompletionEvaluator completion;

    public DegreeEvaluator(CompletionEvaluator completion) {
        this.completion = completion;
    }

    /**
     * {@code programs} holds the plan's programs that are in the catalog. A combination of more than one program, such
     * as two Majors, also needs 12.0 different credits among the courses those programs claim. If the claims fall
     * short, but the courses the programs could count reach 12.0, the result is {@code REVIEW}.
     */
    public DegreeProgress evaluate(Degree degree, AcademicSnapshot snapshot, List<ProgramProgress> programs) {
        CompletionResult rows = completion.evaluate(degree.getCompletion(), snapshot);
        List<ProgramProgress> complete = new ArrayList<>();
        List<ProgramProgress> possible = new ArrayList<>();

        for (ProgramProgress p : programs) {
            if (!degree.countsTowardCombination(p.getProgramCode())) {
                continue;
            }

            Status s = status(p, snapshot);

            if (s == Status.MET) {
                complete.add(p);
            }

            if (s != Status.NOT_MET) {
                possible.add(p);
            }
        }

        Optional<String> unsure = missing(degree, complete, snapshot, true);

        if (unsure.isEmpty()) {
            return new DegreeProgress(degree.getType(), rows, Status.MET, null);
        }

        Optional<String> reason = missing(degree, possible, snapshot, false);

        if (reason.isPresent()) {
            return new DegreeProgress(degree.getType(), rows, Status.NOT_MET, reason.get());
        }

        if (complete.size() == possible.size()) {
            return new DegreeProgress(degree.getType(), rows, Status.REVIEW, unsure.get());
        }

        return new DegreeProgress(degree.getType(), rows, Status.REVIEW,
                "met if your programs needing review are complete and you are admitted to them");
    }

    private static Status status(ProgramProgress p, AcademicSnapshot snapshot) {
        Status s = p.getCompletion().getOverall();

        if (s == Status.MET && !snapshot.getEnrolledPrograms().contains(p.getProgramCode())) {
            return Status.REVIEW;
        }

        return s;
    }

    private static Optional<String> missing(Degree degree, List<ProgramProgress> programs, AcademicSnapshot snapshot,
                                            boolean certain) {
        ProgramCombinationRule rule = degree.getProgramCombinations();
        List<ProgramType> types = programs.stream().map(ProgramProgress::getType).collect(Collectors.toList());

        if (!rule.satisfiedBy(types)) {
            String reason = "needs " + rule.describe() + " completed";

            if (!degree.getEligiblePrograms().isEmpty()) {
                reason += " (one of " + String.join(", ", degree.getEligiblePrograms()) + ")";
            }

            return Optional.of(reason);
        }

        for (ProgramType t : types) {
            if (rule.satisfiedBy(List.of(t))) {
                return Optional.empty();
            }
        }

        return differentCredits(programs, snapshot, certain);
    }

    private static Optional<String> differentCredits(List<ProgramProgress> programs, AcademicSnapshot snapshot,
                                                     boolean certain) {
        Set<String> claimed = new HashSet<>();
        Set<String> usable = new HashSet<>();

        for (ProgramProgress p : programs) {
            for (RowResult r : p.getCompletion().getRows()) {
                claimed.addAll(r.getClaimed());
                usable.addAll(r.getClaimed());
                r.getDetail().ifPresent(d -> usable.addAll(d.allCounted()));
            }
        }

        double most = credits(usable, snapshot);

        if (most < DIFFERENT_CREDITS - 1e-9) {
            return Optional.of("your programs include only " + most + " different credits; they need "
                    + DIFFERENT_CREDITS);
        }

        if (certain && credits(claimed, snapshot) < DIFFERENT_CREDITS - 1e-9) {
            return Optional.of("your programs share courses, so they might not include " + DIFFERENT_CREDITS
                    + " different credits");
        }

        return Optional.empty();
    }

    private static double credits(Set<String> codes, AcademicSnapshot snapshot) {
        double total = 0;

        for (CourseRecord r : snapshot.allRecords()) {
            if (codes.contains(r.getCode())) {
                total += r.getWeight();
            }
        }

        return total;
    }
}
