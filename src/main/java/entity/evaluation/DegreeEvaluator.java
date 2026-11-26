package entity.evaluation;

import entity.catalog.CompletionRow;
import entity.catalog.ProgramType;
import entity.catalog.RowKind;
import entity.degree.Degree;
import entity.degree.ProgramCombinationRule;
import entity.requirement.*;

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
     * as two Majors, also needs 12.0 different credits across those programs. The courses the programs claim give an
     * upper bound on that count. Each program's minimum credits, minus the courses that two programs both need, give a
     * lower bound. If the bounds don't decide it, the result is {@code REVIEW}.
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
        Map<String, Integer> neededBy = new HashMap<>();
        double least = 0;

        for (ProgramProgress p : programs) {
            for (RowResult r : p.getCompletion().getRows()) {
                claimed.addAll(r.getClaimed());
            }

            for (String code : needed(p)) {
                neededBy.merge(code, 1, Integer::sum);
            }

            least += programMinimum(p);
        }

        double most = 0;

        for (CourseRecord r : snapshot.allRecords()) {
            int n = neededBy.getOrDefault(r.getCode(), 0);

            if (claimed.contains(r.getCode())) {
                most += r.getWeight();
            }

            if (n > 1) {
                least -= r.getWeight() * (n - 1);
            }
        }

        if (most < DIFFERENT_CREDITS - 1e-9) {
            return Optional.of("your programs include only " + most + " different credits; they need "
                    + DIFFERENT_CREDITS);
        }

        if (certain && least < DIFFERENT_CREDITS - 1e-9) {
            return Optional.of("your programs share courses, so they might not include " + DIFFERENT_CREDITS
                    + " different credits");
        }

        return Optional.empty();
    }

    private static Set<String> needed(ProgramProgress p) {
        List<RowResult> rows = p.getCompletion().getRows();
        Set<String> codes = new HashSet<>();

        for (RowResult r : rows) {
            if (r.isApplicable() && r.getRow().getKind() == RowKind.ALLOCATION && !coveredByChildren(r, rows)) {
                r.getDetail().ifPresent(d -> codes.addAll(d.allCounted()));
            }
        }

        return codes;
    }

    private static boolean coveredByChildren(RowResult parent, List<RowResult> rows) {
        Optional<Integer> number = Optional.of(parent.getNumber());
        double children = 0;

        for (RowResult r : rows) {
            CompletionRow row = r.getRow();

            if (r.isApplicable() && row.getKind() == RowKind.ALLOCATION && row.getParent().equals(number)) {
                children += row.getReq().map(DegreeEvaluator::minimum).orElse(0.0);
            }
        }

        return children >= parent.getRow().getReq().map(DegreeEvaluator::minimum).orElse(0.0) - 1e-9;
    }

    private static double programMinimum(ProgramProgress p) {
        double total = 0;

        for (RowResult r : p.getCompletion().getRows()) {
            CompletionRow row = r.getRow();

            if (r.isApplicable() && row.isTopLevel() && row.getKind() == RowKind.ALLOCATION) {
                total += row.getReq().map(DegreeEvaluator::minimum).orElse(0.0);
            }
        }

        return total;
    }

    private static double minimum(Requirement req) {
        if (req instanceof CreditsReq) {
            return ((CreditsReq) req).getAmount();
        }

        if (req instanceof CourseReq) {
            String code = ((CourseReq) req).getCode();
            return code.length() >= 7 && code.charAt(6) == 'Y' ? 1.0 : 0.5;
        }

        if (req instanceof AndReq) {
            return ((AndReq) req).getItems().stream().mapToDouble(DegreeEvaluator::minimum).sum();
        }

        if (req instanceof OrReq) {
            return ((OrReq) req).getItems().stream().mapToDouble(DegreeEvaluator::minimum).min().orElse(0);
        }

        if (req instanceof ChooseReq) {
            ChooseReq c = (ChooseReq) req;
            return c.getItems().stream().map(DegreeEvaluator::minimum).sorted().limit(c.getCount())
                    .mapToDouble(d -> d).sum();
        }

        return 0;
    }
}
