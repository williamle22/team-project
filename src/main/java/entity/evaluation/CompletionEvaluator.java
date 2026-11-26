package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import entity.requirement.*;

import java.util.*;

/**
 * Checks a program's or degree's completion requirements against a snapshot. Sibling allocation rows don't share a
 * course unless one lists the other in its overlaps, or both are options of a choice row. Rows that refer to other
 * rows, and choice options, pick courses last. The other rows pick in order of fewest candidate courses. A met row
 * claims only the courses it needs, and an unmet row claims every course it matches. A row that's {@code NOT_MET} with
 * the courses left to it, but not with every course, is {@code REVIEW}. If the contested courses, which more than one
 * sibling could use, have at most 256 assignments, the evaluator then tries each one. It keeps the assignment with the
 * fewest rows that aren't met, if that beats the first result. Constraint and limit child rows check only the courses
 * their parent counts, unless a row is transcript-scoped. A row applies only to students in its audience. An exceeded
 * limit raises a parent credit requirement by the excess. Under any other parent requirement, the evaluator drops the
 * excess courses and checks the parent again, which gives {@code MET} or {@code REVIEW}. A limit whose parent has no
 * requirement gives {@code REVIEW}. Options of a choice row count only through the choice row.
 */
public final class CompletionEvaluator {
    public CompletionResult evaluate(Completion completion, AcademicSnapshot snapshot) {
        Walk walk = new Walk(completion, snapshot, true);
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

        return new CompletionResult(walk.results, Status.allOf(statuses));
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
        private static final int MAX_ASSIGNMENTS = 256;
        private static final String DIFFERENT_ALLOCATION = "may be satisfiable with a different allocation";

        private final Completion completion;
        private final AcademicSnapshot snapshot;
        private final boolean search;
        private final CompletionResolver resolver;
        private final Set<String> all = new LinkedHashSet<>();
        private final List<RowResult> results = new ArrayList<>();
        private final Map<Integer, RowResult> finished = new HashMap<>();
        private final Set<Integer> options = new HashSet<>();

        Walk(Completion completion, AcademicSnapshot snapshot, boolean search) {
            this.completion = completion;
            this.snapshot = snapshot;
            this.search = search;
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

        private List<RowResult> evaluateSiblings(List<CompletionRow> siblings, Set<String> available) {
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
            int start = results.size();
            List<RowResult> done = greedy(order, available, demand);

            if (!search || noDifferentAllocation(results.subList(start, results.size()))) {
                return done;
            }

            Map<String, List<CompletionRow>> contested = new TreeMap<>();

            for (CompletionRow row : order) {
                for (String code : wants.get(row)) {
                    if (demand.get(code) > 1) {
                        contested.computeIfAbsent(code, c -> new ArrayList<>()).add(row);
                    }
                }
            }

            if (count(contested) > MAX_ASSIGNMENTS) {
                return done;
            }

            Map<String, CompletionRow> best = null;
            int fewest = missing(done);

            for (Map<String, CompletionRow> owners : assignments(contested)) {
                int m = missing(fork().assigned(order, available, demand, owners));

                if (m < fewest) {
                    best = owners;
                    fewest = m;
                }
            }

            if (best == null) {
                return done;
            }

            results.subList(start, results.size()).clear();

            return assigned(order, available, demand, best);
        }

        private List<RowResult> greedy(List<CompletionRow> order, Set<String> available,
                                       Map<String, Integer> demand) {
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

        private List<RowResult> assigned(List<CompletionRow> order, Set<String> available,
                                         Map<String, Integer> demand, Map<String, CompletionRow> owners) {
            List<RowResult> done = new ArrayList<>();

            for (CompletionRow row : order) {
                Set<String> mine = new LinkedHashSet<>(available);

                owners.forEach((code, owner) -> {
                    if (owner != row && !mayShare(owner, row)) {
                        mine.remove(code);
                    }
                });

                done.add(evaluate(row, mine, demand));
            }

            return done;
        }

        private int missing(List<RowResult> done) {
            int n = 0;

            for (RowResult r : done) {
                if (counts(r) && r.getStatus() != Status.MET) {
                    n++;
                }
            }

            return n;
        }

        private static boolean noDifferentAllocation(List<RowResult> rows) {
            return rows.stream().noneMatch(r -> r.getReason().filter(DIFFERENT_ALLOCATION::equals).isPresent());
        }

        private static long count(Map<String, List<CompletionRow>> contested) {
            long n = 1;

            for (List<CompletionRow> rows : contested.values()) {
                n = Math.min(n * rows.size(), MAX_ASSIGNMENTS + 1);
            }

            return n;
        }

        private static List<Map<String, CompletionRow>> assignments(Map<String, List<CompletionRow>> contested) {
            List<Map<String, CompletionRow>> all = List.of(Map.of());

            for (Map.Entry<String, List<CompletionRow>> e : contested.entrySet()) {
                List<Map<String, CompletionRow>> next = new ArrayList<>();

                for (Map<String, CompletionRow> partial : all) {
                    for (CompletionRow row : e.getValue()) {
                        Map<String, CompletionRow> owners = new HashMap<>(partial);
                        owners.put(e.getKey(), row);
                        next.add(owners);
                    }
                }

                all = next;
            }

            return all;
        }

        private Walk fork() {
            Walk w = new Walk(completion, snapshot, false);
            finished.values().forEach(w::finish);
            w.results.clear();

            return w;
        }

        private RowResult evaluate(CompletionRow row, Set<String> pool, Map<String, Integer> demand) {
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
                        reason = DIFFERENT_ALLOCATION;
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

        private boolean counts(RowResult r) {
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

            double over = excess(under, cap, max);

            if (over <= 1e-9) {
                return new RowResult(row, true, Status.MET, null, null, List.of());
            }

            String reason = cap.getFilter().isPerDesignator()
                    ? over + " credits go past " + max + " in one designator"
                    : matched + " credits fall under this limit; only " + max + " can count";

            if (parent.isPresent()) {
                CreditsReq p = parent.get();
                CreditsReq raised = new CreditsReq(p.getAmount() + over, p.getMaxAmount().orElse(null),
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

            for (int n = under.size(); n > 0 && excess(under.subList(0, n), cap, max) > 1e-9; n--) {
                kept.remove(under.get(n - 1).getCode());
            }

            Status status = check(snapshot.restrictedTo(kept), parentReq.get()).getStatus();

            return new RowResult(row, true, status == Status.MET ? Status.MET : Status.REVIEW, reason, null,
                    List.of());
        }

        private static double excess(List<CourseRecord> under, CreditsReq cap, double max) {
            Map<String, Double> credits = new HashMap<>();

            for (CourseRecord r : under) {
                String group = cap.getFilter().isPerDesignator() ? r.getCourse().getDesignator() : "";
                credits.merge(group, cap.getWeight().orElse(r.getWeight()), Double::sum);
            }

            double excess = 0;

            for (double c : credits.values()) {
                excess += Math.max(0, c - max);
            }

            return excess;
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
