package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import entity.requirement.*;

import java.util.*;

/**
 * Checks a program's or degree's completion requirements against a snapshot. An allocation row claims the courses it
 * counts, and its constraint and limit child rows check only those courses. A transcript-scoped row checks every course
 * instead. A row applies only to students in its audience. An exceeded limit raises its parent's credit requirement by
 * the excess, and a limit without a parent credit requirement gives {@code REVIEW}. Options of a choice row count only
 * through the choice row. This is Level 1, which doesn't assign each course to one row. If two unrelated allocation
 * rows count the same course, the result is {@code REVIEW}, with a warning.
 */
public final class CompletionEvaluator {
    public CompletionResult evaluate(Completion completion, AcademicSnapshot snapshot) {
        Walk walk = new Walk(completion, snapshot);
        List<Status> statuses = new ArrayList<>();

        for (CompletionRow row : completion.topLevelRows()) {
            RowResult r = walk.evaluate(row, null);

            if (walk.counts(r)) {
                statuses.add(r.getStatus());
            }
        }

        List<String> warnings = walk.doubleCounting();

        if (!warnings.isEmpty()) {
            statuses.add(Status.REVIEW);
        }

        return new CompletionResult(walk.results, Status.allOf(statuses), warnings);
    }

    private static final class Walk {
        private final Completion completion;
        private final AcademicSnapshot snapshot;
        private final CompletionResolver resolver;
        private final List<RowResult> results = new ArrayList<>();
        private final Set<Integer> options = new HashSet<>();

        Walk(Completion completion, AcademicSnapshot snapshot) {
            this.completion = completion;
            this.snapshot = snapshot;
            this.resolver = new CompletionResolver(completion, snapshot);

            for (CompletionRow row : completion.getRequirements()) {
                if (row.getKind() == RowKind.CHOICE) {
                    collectRows(row.getReq().orElseThrow(), options);
                }
            }
        }

        RowResult evaluate(CompletionRow row, Set<String> pool) {
            Optional<EvaluationNode> audience = row.getAudience()
                    .map(a -> new RequisiteEvaluator(snapshot, true, resolver).evaluate(a));

            if (audience.isPresent() && audience.get().getStatus() == Status.NOT_MET) {
                EvaluationNode who = audience.get();

                return finish(new RowResult(row, false, Status.NOT_MET, who.getReason().orElse(null), who, List.of()));
            }

            if (row.getKind() == RowKind.LIMIT) {
                return finish(limit(row, pool));
            }

            List<CompletionRow> children = completion.childrenOf(row.getNumber());
            List<Status> statuses = new ArrayList<>();
            Set<String> claimed = new LinkedHashSet<>();

            for (CompletionRow child : children) {
                if (child.getKind() == RowKind.ALLOCATION) {
                    RowResult r = evaluate(child, null);

                    if (counts(r)) {
                        statuses.add(r.getStatus());
                    }

                    claimed.addAll(r.getClaimed());
                }
            }

            EvaluationNode detail = null;

            if (row.getReq().isPresent()) {
                detail = new RequisiteEvaluator(scope(row, pool), true, resolver).evaluate(row.getReq().get());
                statuses.add(detail.getStatus());

                if (row.getKind() == RowKind.ALLOCATION) {
                    claimed.addAll(detail.allCounted());
                }
            }

            for (CompletionRow child : children) {
                if (child.getKind() != RowKind.ALLOCATION) {
                    RowResult r = evaluate(child, claimed);

                    if (counts(r)) {
                        statuses.add(r.getStatus());
                    }
                }
            }

            return finish(new RowResult(row, true, Status.allOf(statuses), null, detail, new ArrayList<>(claimed)));
        }

        List<String> doubleCounting() {
            List<RowResult> allocations = new ArrayList<>();

            for (RowResult r : results) {
                if (r.isApplicable() && r.getRow().getKind() == RowKind.ALLOCATION && r.getDetail().isPresent()) {
                    allocations.add(r);
                }
            }

            List<String> warnings = new ArrayList<>();

            for (int i = 0; i < allocations.size(); i++) {
                for (int j = i + 1; j < allocations.size(); j++) {
                    RowResult a = allocations.get(i);
                    RowResult b = allocations.get(j);

                    if (mayShare(a.getRow(), b.getRow())) {
                        continue;
                    }

                    Set<String> shared = a.getDetail().get().allCounted();
                    shared.retainAll(b.getDetail().get().allCounted());

                    for (String code : shared) {
                        warnings.add(code + " may be counting toward both requirement " + a.getNumber()
                                + " and requirement " + b.getNumber());
                    }
                }
            }

            return warnings;
        }

        private static void collectRows(Requirement req, Set<Integer> rows) {
            List<Requirement> items = List.of();

            if (req instanceof RowRefReq) {
                rows.addAll(((RowRefReq) req).getRows());
            } else if (req instanceof OrReq) {
                items = ((OrReq) req).getItems();
            } else if (req instanceof ChooseReq) {
                items = ((ChooseReq) req).getItems();
            } else if (req instanceof AndReq) {
                items = ((AndReq) req).getItems();
            }

            for (Requirement item : items) {
                collectRows(item, rows);
            }
        }

        private boolean mayShare(CompletionRow a, CompletionRow b) {
            Set<Integer> upA = lineage(a);
            Set<Integer> upB = lineage(b);

            return upA.contains(b.getNumber())
                    || !Collections.disjoint(overlapsOf(upA), upB)
                    || !Collections.disjoint(overlapsOf(upB), upA)
                    || inDifferentOptions(upA, upB);
        }

        private Set<Integer> lineage(CompletionRow row) {
            Set<Integer> numbers = new HashSet<>();
            Optional<CompletionRow> current = Optional.of(row);

            while (current.isPresent()) {
                numbers.add(current.get().getNumber());
                current = current.get().getParent().flatMap(completion::row);
            }

            return numbers;
        }

        private Set<Integer> overlapsOf(Set<Integer> rows) {
            Set<Integer> overlaps = new HashSet<>();

            for (int n : rows) {
                overlaps.addAll(completion.row(n).orElseThrow().getOverlaps());
            }

            return overlaps;
        }

        private boolean inDifferentOptions(Set<Integer> upA, Set<Integer> upB) {
            Set<Integer> optionsA = new HashSet<>(upA);
            optionsA.retainAll(options);
            Set<Integer> optionsB = new HashSet<>(upB);
            optionsB.retainAll(options);

            return !optionsA.isEmpty() && !optionsB.isEmpty() && Collections.disjoint(optionsA, optionsB);
        }

        private boolean counts(RowResult r) {
            return r.isApplicable() && !options.contains(r.getNumber());
        }

        private RowResult limit(CompletionRow row, Set<String> pool) {
            CreditsReq cap = (CreditsReq) row.getReq().orElseThrow();
            double max = cap.getMaxAmount().orElseThrow();
            Optional<CreditsReq> parent = row.getParent().flatMap(completion::row).flatMap(CompletionRow::getReq)
                    .filter(r -> r instanceof CreditsReq).map(r -> (CreditsReq) r);
            double matched = 0;

            for (CourseRecord r : scope(row, pool).allRecords()) {
                boolean counted = parent.isEmpty()
                        || CourseMatcher.matches(parent.get().getFilter(), r, resolver) != Match.NO;

                if (counted && CourseMatcher.matches(cap.getFilter(), r, resolver) != Match.NO) {
                    matched += cap.getWeight().orElse(r.getCourse().getWeight());
                }
            }

            if (matched <= max + 1e-9) {
                return new RowResult(row, true, Status.MET, null, null, List.of());
            }

            String reason = matched + " credits fall under this limit; only " + max + " can count";

            if (parent.isEmpty()) {
                return new RowResult(row, true, Status.REVIEW, reason, null, List.of());
            }

            CreditsReq p = parent.get();
            CreditsReq raised = new CreditsReq(p.getAmount() + matched - max, p.getMaxAmount().orElse(null),
                    p.getWeight().orElse(null), p.getFilter(), p.isConcurrent());
            Status status = new RequisiteEvaluator(snapshot, true, resolver).evaluate(raised).getStatus();

            return new RowResult(row, true, status, reason, null, List.of());
        }

        private AcademicSnapshot scope(CompletionRow row, Set<String> pool) {
            return pool == null || row.isTranscriptScope() ? snapshot : snapshot.restrictedTo(pool);
        }

        private RowResult finish(RowResult result) {
            results.add(result);
            resolver.record(result);

            return result;
        }
    }
}
