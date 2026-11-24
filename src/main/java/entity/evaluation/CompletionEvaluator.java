package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CompletionEvaluator {
    public CompletionResult evaluate(Completion completion, AcademicSnapshot snapshot) {
        Walk walk = new Walk(completion, snapshot);
        List<Status> statuses = new ArrayList<>();

        for (CompletionRow row : completion.topLevelRows()) {
            statuses.add(walk.evaluate(row, null).getStatus());
        }

        return new CompletionResult(walk.results, Status.allOf(statuses), List.of());
    }

    private static final class Walk {
        private final Completion completion;
        private final AcademicSnapshot snapshot;
        private final CompletionResolver resolver;
        private final List<RowResult> results = new ArrayList<>();

        Walk(Completion completion, AcademicSnapshot snapshot) {
            this.completion = completion;
            this.snapshot = snapshot;
            this.resolver = new CompletionResolver(completion, snapshot);
        }

        RowResult evaluate(CompletionRow row, Set<String> pool) {
            List<CompletionRow> children = completion.childrenOf(row.getNumber());
            List<Status> statuses = new ArrayList<>();
            Set<String> claimed = new LinkedHashSet<>();

            for (CompletionRow child : children) {
                if (child.getKind() == RowKind.ALLOCATION) {
                    RowResult r = evaluate(child, null);
                    statuses.add(r.getStatus());
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
                    statuses.add(evaluate(child, claimed).getStatus());
                }
            }

            return finish(new RowResult(row, true, Status.allOf(statuses), null, detail, new ArrayList<>(claimed)));
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
