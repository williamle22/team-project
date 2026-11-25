package entity.evaluation;

import entity.catalog.CompletionRow;
import entity.catalog.ProgramType;
import entity.catalog.RowKind;
import entity.degree.Degree;
import entity.degree.ProgramCombinationRule;
import entity.requirement.*;

import java.util.*;
import java.util.stream.Collectors;

public final class DegreeEvaluator {
    private static final double DIFFERENT_CREDITS = 12.0;

    private final CompletionEvaluator completion;

    public DegreeEvaluator(CompletionEvaluator completion) {
        this.completion = completion;
    }

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
        Map<String, Integer> matchedBy = new HashMap<>();
        double least = 0;

        for (ProgramProgress p : programs) {
            Set<String> codes = new HashSet<>();

            for (RowResult r : p.getCompletion().getRows()) {
                codes.addAll(r.getClaimed());
            }

            for (String code : codes) {
                matchedBy.merge(code, 1, Integer::sum);
            }

            least += programMinimum(p);
        }

        double most = 0;

        for (CourseRecord r : snapshot.allRecords()) {
            int n = matchedBy.getOrDefault(r.getCode(), 0);

            if (n > 0) {
                most += r.getCourse().getWeight();
                least -= r.getCourse().getWeight() * (n - 1);
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
