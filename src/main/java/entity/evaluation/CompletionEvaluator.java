package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import entity.requirement.*;

import java.util.*;

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

        return new CompletionResult(walk.results, Status.allOf(statuses), List.of());
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
