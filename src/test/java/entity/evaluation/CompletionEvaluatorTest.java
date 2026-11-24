package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import entity.requirement.CourseFilter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.asmin1001;
import static testutil.CatalogFixtures.completion;
import static testutil.CatalogFixtures.completionRow;
import static testutil.CatalogFixtures.credits;
import static testutil.CatalogFixtures.creditsIn;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;

class CompletionEvaluatorTest {
    private static final Completion ASMIN1001 = asmin1001();
    private static final String[] ASMIN1001_COMPLETE = {
            "PHL281H1", "PHL245H1", "PHL380H1", "PHL271H1", "PHL275H1", "PHL365H1", "PHL232H1", "PHL233H1"};

    private final CompletionEvaluator evaluator = new CompletionEvaluator();

    private static AcademicSnapshot taken(String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder();
        for (String code : codes) {
            b.taken(record(code));
        }
        return b.build();
    }

    private static Status status(CompletionResult result, int row) {
        return result.row(row).orElseThrow().getStatus();
    }

    private static CompletionRow transcriptConstraint(boolean transcriptScope) {
        return new CompletionRow(2, RowKind.CONSTRAINT, null, null, 1, List.of(),
                credits(0.5, CourseFilter.builder().levels(List.of(300)).build()), null, transcriptScope, List.of(),
                "raw");
    }

    @Test
    void emptyTranscriptNothingMet() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken());

        assertEquals(NOT_MET, r.getOverall());
        for (int row = 1; row <= 6; row++) {
            assertEquals(NOT_MET, status(r, row), "row " + row);
        }
        assertEquals(List.of(), r.getWarnings());
    }

    @Test
    void partialTranscript() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken("PHL281H1"));

        assertEquals(MET, status(r, 3));
        assertEquals(NOT_MET, status(r, 4));
        assertEquals(NOT_MET, status(r, 1));
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void completeTranscriptMet() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken(ASMIN1001_COMPLETE));

        for (int row = 1; row <= 6; row++) {
            assertEquals(MET, status(r, row), "row " + row);
        }
        assertEquals(MET, r.getOverall());
    }

    @Test
    void childFailureFailsParent() {
        String[] noGroup1 = {
                "PHL281H1", "PHL245H1", "PHL366H1", "PHL271H1", "PHL275H1", "PHL365H1", "PHL232H1", "PHL233H1"};

        CompletionResult r = evaluator.evaluate(ASMIN1001, taken(noGroup1));

        assertEquals(NOT_MET, status(r, 5));
        assertEquals(MET, r.row(1).orElseThrow().getDetail().orElseThrow().getStatus());
        assertEquals(NOT_MET, status(r, 1));
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void constraintUsesParentPool() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken(
                "PHL281H1", "PHL245H1", "PHL271H1", "PHL275H1", "PHL232H1", "PHL233H1", "PHL200H1", "PHL201H1",
                "CSC369H1", "CSC373H1"));

        assertEquals(NOT_MET, status(r, 2));
    }

    @Test
    void transcriptScopeConstraintLooksOutsidePool() {
        CompletionRow parent = completionRow(1, RowKind.ALLOCATION, null, null, creditsIn(0.5, "PHL"), "raw");
        AcademicSnapshot snapshot = taken("PHL281H1", "CSC369H1");

        CompletionResult inPool = evaluator.evaluate(completion(List.of(parent, transcriptConstraint(false))),
                snapshot);
        CompletionResult transcript = evaluator.evaluate(completion(List.of(parent, transcriptConstraint(true))),
                snapshot);

        assertEquals(NOT_MET, status(inPool, 2));
        assertEquals(MET, status(transcript, 2));
    }

    @Test
    void optionGroupCombinesChildren() {
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, "Czech", null, null, "raw"),
                completionRow(2, RowKind.ALLOCATION, null, 1, req("SLA105Y1"), "raw"),
                completionRow(3, RowKind.ALLOCATION, null, 1, req("SLA200H1"), "raw")));

        CompletionResult partial = evaluator.evaluate(c, taken("SLA105Y1"));
        CompletionResult both = evaluator.evaluate(c, taken("SLA105Y1", "SLA200H1"));

        assertEquals(NOT_MET, status(partial, 1));
        assertTrue(partial.row(1).orElseThrow().getDetail().isEmpty());
        assertEquals(List.of("SLA105Y1"), partial.row(1).orElseThrow().getClaimed());
        assertEquals(MET, status(both, 1));
        assertEquals(MET, both.getOverall());
    }

    @Test
    void parentClaimsChildCourses() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken(ASMIN1001_COMPLETE));

        assertEquals(List.of("PHL281H1"), r.row(3).orElseThrow().getClaimed());
        assertTrue(r.row(1).orElseThrow().getClaimed().containsAll(List.of(ASMIN1001_COMPLETE)));
        assertEquals(List.of(), r.row(2).orElseThrow().getClaimed());
    }

    @Test
    void resultsInEvaluationOrder() {
        CompletionResult r = evaluator.evaluate(ASMIN1001, taken());

        List<Integer> order = r.getRows().stream().map(RowResult::getNumber).collect(Collectors.toList());

        assertEquals(List.of(3, 4, 5, 6, 2, 1), order);
    }

    @Test
    void noRowsIsMet() {
        assertEquals(MET, evaluator.evaluate(completion(List.of()), taken()).getOverall());
    }
}
