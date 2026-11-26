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
        List<CompletionRow> top = completion.topLevelRows();
        List<Status> statuses = new ArrayList<>();

        for (RowResult r : walk.evaluateSiblings(allocations(top), walk.all)) {
            if (walk.counts(r)) {
                statuses.add(r.getStatus());
            }
        }

        for (CompletionRow row : top) {
            if (row.getKind() != RowKind.ALLOCATION) {
                RowResult r = walk.evaluate(row, null, Map.of());

                if (walk.counts(r)) {
                    statuses.add(r.getStatus());
                }
            }
        }

        List<CompletionRow> order = completion.getRequirements();
        walk.results.sort(Comparator.comparingInt(r -> order.indexOf(r.getRow())));

        return new CompletionResult(walk.results, Status.allOf(statuses), List.of());
    }

    private static List<CompletionRow> allocations(List<CompletionRow> rows) {
        List<CompletionRow> allocations = new ArrayList<>();

        for (CompletionRow row : rows) {
            if (row.getKind() == RowKind.ALLOCATION) {
                allocations.add(row);
            }
        }

        return allocations;
    }

    private static final class Walk {
        private final Completion completion;
        private final AcademicSnapshot snapshot;
        private final CompletionResolver resolver;
        private final Set<String> all = new LinkedHashSet<>();
        private final List<RowResult> results = new ArrayList<>();
        private final Map<Integer, RowResult> finished = new HashMap<>();
        private final Set<Integer> options = new HashSet<>();

        Walk(Completion completion, AcademicSnapshot snapshot) {
            this.completion = completion;
            this.snapshot = snapshot;
            this.resolver = new CompletionResolver(completion, snapshot);

            for (CourseRecord r : snapshot.allRecords()) {
                all.add(r.getCode());
            }

            for (CompletionRow row : completion.getRequirements()) {
                if (row.getKind() == RowKind.CHOICE) {
                    collectRows(row.getReq().orElseThrow(), options);
                }
            }
        }

        List<RowResult> evaluateSiblings(List<CompletionRow> siblings, Set<String> available) {
            Map<CompletionRow, Set<String>> wants = new HashMap<>();
            Map<String, Integer> demand = new HashMap<>();

            for (CompletionRow row : siblings) {
                wants.put(row, candidates(row, available));

                for (String code : wants.get(row)) {
                    demand.merge(code, 1, Integer::sum);
                }
            }

            List<CompletionRow> order = new ArrayList<>(siblings);
            order.sort(Comparator.comparing(this::goesLast)
                    .thenComparingInt(row -> wants.get(row).size()));
            List<RowResult> done = new ArrayList<>();

            for (CompletionRow row : order) {
                Set<String> mine = new LinkedHashSet<>(available);

                for (RowResult d : done) {
                    if (!mayShare(d.getRow(), row)) {
                        d.getClaimed().forEach(mine::remove);
                    }
                }

                done.add(evaluate(row, mine, demand));
            }

            return done;
        }

        RowResult evaluate(CompletionRow row, Set<String> pool, Map<String, Integer> demand) {
            Optional<EvaluationNode> audience = row.getAudience()
                    .map(a -> check(snapshot, a));

            if (audience.isPresent() && audience.get().getStatus() == Status.NOT_MET) {
                EvaluationNode who = audience.get();

                return finish(new RowResult(row, false, Status.NOT_MET, who.getReason().orElse(null), who, List.of()));
            }

            if (row.getKind() == RowKind.LIMIT) {
                return finish(limit(row, pool));
            }

            List<CompletionRow> children = completion.childrenOf(row.getNumber());
            List<Status> statuses = new ArrayList<>();
            Set<String> fromChildren = new LinkedHashSet<>();

            for (RowResult r : evaluateSiblings(allocations(children), pool == null ? all : pool)) {
                if (counts(r)) {
                    statuses.add(r.getStatus());
                }

                fromChildren.addAll(r.getClaimed());
            }

            EvaluationNode detail = null;
            String reason = null;
            Set<String> matched = new LinkedHashSet<>(fromChildren);

            if (row.getReq().isPresent()) {
                Requirement req = row.getReq().get();
                detail = check(scope(row, pool), req);
                Status own = detail.getStatus();

                if (row.getKind() == RowKind.ALLOCATION) {
                    matched.addAll(detail.allCounted());

                    if (own == Status.NOT_MET && check(snapshot, req).getStatus() != Status.NOT_MET) {
                        own = Status.REVIEW;
                        reason = "may be satisfiable with a different allocation";
                    }
                }

                statuses.add(own);
            }

            for (CompletionRow child : children) {
                if (child.getKind() != RowKind.ALLOCATION) {
                    RowResult r = evaluate(child, matched, Map.of());

                    if (counts(r)) {
                        statuses.add(r.getStatus());
                    }
                }
            }

            Status status = Status.allOf(statuses);
            List<String> claimed = new ArrayList<>(fromChildren);

            if (row.getKind() == RowKind.ALLOCATION) {
                claimed = status == Status.NOT_MET || reason != null ? new ArrayList<>(matched)
                        : trim(row, matched, fromChildren, demand, pool);
            }

            return finish(new RowResult(row, true, status, reason, detail, claimed));
        }

        boolean counts(RowResult r) {
            return r.isApplicable() && !options.contains(r.getNumber());
        }

        private Set<String> candidates(CompletionRow row, Set<String> available) {
            Set<String> codes = new HashSet<>();
            AcademicSnapshot scope = snapshot.restrictedTo(available);

            row.getReq().ifPresent(r -> codes.addAll(check(scope, r).allCounted()));

            for (CompletionRow child : allocations(completion.childrenOf(row.getNumber()))) {
                codes.addAll(candidates(child, available));
            }

            return codes;
        }

        private boolean goesLast(CompletionRow row) {
            Set<Integer> refs = new HashSet<>();
            row.getReq().ifPresent(r -> collectRows(r, refs));

            return !refs.isEmpty() || options.contains(row.getNumber());
        }

        private boolean mayShare(CompletionRow a, CompletionRow b) {
            return a.getOverlaps().contains(b.getNumber()) || b.getOverlaps().contains(a.getNumber())
                    || options.containsAll(List.of(a.getNumber(), b.getNumber()));
        }

        private List<String> trim(CompletionRow row, Set<String> matched, Set<String> keep,
                                  Map<String, Integer> demand, Set<String> pool) {
            Set<String> wanted = new HashSet<>();

            for (CompletionRow child : allocations(completion.childrenOf(row.getNumber()))) {
                wanted.addAll(candidates(child, pool));
            }

            List<String> order = new ArrayList<>(matched);
            order.removeAll(keep);
            order.sort(Comparator.comparingInt((String code) -> -demand.getOrDefault(code, 0))
                    .thenComparing(wanted::contains));
            List<Status> before = checks(row, matched);
            Set<String> dropped = new HashSet<>();

            for (String code : order) {
                dropped.add(code);
                Set<String> claim = new HashSet<>(matched);
                claim.removeAll(dropped);

                if (!checks(row, claim).equals(before)) {
                    dropped.remove(code);
                }
            }

            List<String> claimed = new ArrayList<>(matched);
            claimed.removeAll(dropped);

            return claimed;
        }

        private List<Status> checks(CompletionRow row, Set<String> claim) {
            List<Status> statuses = new ArrayList<>();
            AcademicSnapshot restricted = snapshot.restrictedTo(claim);

            row.getReq().ifPresent(r -> statuses.add(check(restricted, r).getStatus()));

            for (CompletionRow child : completion.childrenOf(row.getNumber())) {
                if (child.getKind() != RowKind.ALLOCATION && counts(finished.get(child.getNumber()))) {
                    statuses.add(child.getKind() == RowKind.LIMIT ? limit(child, claim).getStatus()
                            : check(scope(child, claim), child.getReq().orElseThrow()).getStatus());
                }
            }

            return statuses;
        }

        private static void collectRows(Requirement req, Set<Integer> rows) {
            List<Requirement> items = List.of();

            if (req instanceof RowRefReq) {
                rows.addAll(((RowRefReq) req).getRows());
            } else if (req instanceof CreditsReq) {
                rows.addAll(((CreditsReq) req).getFilter().getRequirements());
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

        private RowResult limit(CompletionRow row, Set<String> pool) {
            CreditsReq cap = (CreditsReq) row.getReq().orElseThrow();
            double max = cap.getMaxAmount().orElseThrow();
            Optional<Requirement> parentReq = row.getParent().flatMap(completion::row).flatMap(CompletionRow::getReq);
            Optional<CreditsReq> parent = parentReq.filter(r -> r instanceof CreditsReq).map(r -> (CreditsReq) r);
            AcademicSnapshot scope = scope(row, pool);
            List<CourseRecord> under = new ArrayList<>();
            double matched = 0;

            for (CourseRecord r : scope.allRecords()) {
                boolean counted = parent.isEmpty()
                        || CourseMatcher.matches(parent.get().getFilter(), r, resolver) != Match.NO;

                if (counted && CourseMatcher.matches(cap.getFilter(), r, resolver) != Match.NO) {
                    matched += cap.getWeight().orElse(r.getWeight());
                    under.add(r);
                }
            }

            if (matched <= max + 1e-9) {
                return new RowResult(row, true, Status.MET, null, null, List.of());
            }

            String reason = matched + " credits fall under this limit; only " + max + " can count";

            if (parent.isPresent()) {
                CreditsReq p = parent.get();
                CreditsReq raised = new CreditsReq(p.getAmount() + matched - max, p.getMaxAmount().orElse(null),
                        p.getWeight().orElse(null), p.getFilter(), p.isConcurrent());
                Status status = check(snapshot, raised).getStatus();

                return new RowResult(row, true, status, reason, null, List.of());
            }

            if (parentReq.isEmpty()) {
                return new RowResult(row, true, Status.REVIEW, reason, null, List.of());
            }

            Set<String> kept = new HashSet<>();

            for (CourseRecord r : scope.allRecords()) {
                kept.add(r.getCode());
            }

            for (int i = under.size() - 1; i >= 0 && matched > max + 1e-9; i--) {
                kept.remove(under.get(i).getCode());
                matched -= cap.getWeight().orElse(under.get(i).getWeight());
            }

            Status status = check(snapshot.restrictedTo(kept), parentReq.get()).getStatus();

            return new RowResult(row, true, status == Status.MET ? Status.MET : Status.REVIEW, reason, null,
                    List.of());
        }

        private EvaluationNode check(AcademicSnapshot scope, Requirement req) {
            return new RequisiteEvaluator(scope, snapshot, true, resolver).evaluate(req);
        }

        private AcademicSnapshot scope(CompletionRow row, Set<String> pool) {
            return pool == null || row.isTranscriptScope() ? snapshot : snapshot.restrictedTo(pool);
        }

        private RowResult finish(RowResult result) {
            results.add(result);
            finished.put(result.getNumber(), result);
            resolver.record(result);

            return result;
        }
    }
}
