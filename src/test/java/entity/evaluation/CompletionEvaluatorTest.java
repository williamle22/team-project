package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import entity.requirement.CourseFilter;
import entity.requirement.CreditsReq;
import entity.requirement.ProfileReq;
import entity.requirement.Requirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.asmin0667;
import static testutil.CatalogFixtures.asmin1001;
import static testutil.CatalogFixtures.completion;
import static testutil.CatalogFixtures.completionRow;
import static testutil.CatalogFixtures.credits;
import static testutil.CatalogFixtures.creditsIn;
import static testutil.CatalogFixtures.or;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.text;

class CompletionEvaluatorTest {
    private static final Completion ASMIN1001 = asmin1001();
    private static final String[] ASMIN1001_COMPLETE = {
            "PHL281H1", "PHL245H1", "PHL380H1", "PHL271H1", "PHL275H1", "PHL365H1", "PHL232H1", "PHL233H1"};
    private static final Completion ASMIN0667 = asmin0667();
    private static final String ARTS_AND_SCIENCE = "Visual Studies Minor students in the Faculty of Arts & Science";

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

    @Test
    void limitWithinCapIsMet() {
        CompletionResult r = evaluator.evaluate(ASMIN0667, taken(
                "HPS100H1", "HPS110H1", "HPS200H1", "HPS201H1", "HPS202H1", "HPS203H1", "HPS300H1", "HPS301H1"));

        assertEquals(MET, status(r, 2));
        assertTrue(r.row(2).orElseThrow().getReason().isEmpty());
        assertEquals(MET, status(r, 1));
        assertEquals(MET, r.getOverall());
    }

    @Test
    void limitOverCapStillMetWithEnoughCredits() {
        CompletionResult r = evaluator.evaluate(ASMIN0667, taken("HPS100H1", "HPS110H1", "HPS120H1", "HPS200H1",
                "HPS201H1", "HPS202H1", "HPS203H1", "HPS300H1", "HPS301H1"));

        RowResult limit = r.row(2).orElseThrow();
        assertEquals(MET, limit.getStatus());
        assertEquals("1.5 credits fall under this limit; only 1.0 can count", limit.getReason().orElseThrow());
        assertEquals(MET, status(r, 1));
        assertEquals(MET, r.getOverall());
    }

    @Test
    void limitOverCapLeavesParentShort() {
        CompletionResult r = evaluator.evaluate(ASMIN0667, taken(
                "HPS100H1", "HPS110H1", "HPS120H1", "HPS200H1", "HPS201H1", "HPS202H1", "HPS300H1", "HPS301H1"));

        RowResult limit = r.row(2).orElseThrow();
        assertEquals(NOT_MET, limit.getStatus());
        assertEquals("1.5 credits fall under this limit; only 1.0 can count", limit.getReason().orElseThrow());
        assertEquals(MET, r.row(1).orElseThrow().getDetail().orElseThrow().getStatus());
        assertEquals(NOT_MET, status(r, 1));
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void limitDoesntRescueNotMetParent() {
        CompletionResult r = evaluator.evaluate(ASMIN0667, taken("HPS100H1", "HPS110H1", "HPS120H1"));

        assertEquals(NOT_MET, status(r, 2));
        assertEquals(NOT_MET, status(r, 1));
    }

    @Test
    void limitUnderOrParentGivesReview() {
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, hpsOrPermission(), "raw"),
                completionRow(2, RowKind.LIMIT, null, 1, hpsCap(), "raw")));

        CompletionResult r = evaluator.evaluate(c, taken("HPS100H1"));

        assertEquals(REVIEW, status(r, 2));
        assertEquals("0.5 credits fall under this limit; only 0.0 can count",
                r.row(2).orElseThrow().getReason().orElseThrow());
        assertEquals(REVIEW, status(r, 1));
    }

    @Test
    void limitOnlyCountsParentPool() {
        CompletionResult r = evaluator.evaluate(cscLimit(false, hpsOrPermission()),
                taken("HPS100H1", "CSC108H1", "CSC148H1"));

        assertEquals(MET, status(r, 2));
    }

    @Test
    void transcriptScopeLimitCountsWholeTranscript() {
        CompletionResult r = evaluator.evaluate(cscLimit(true, hpsOrPermission()),
                taken("HPS100H1", "CSC108H1", "CSC148H1"));

        assertEquals(REVIEW, status(r, 2));
        assertEquals("1.0 credits fall under this limit; only 0.5 can count",
                r.row(2).orElseThrow().getReason().orElseThrow());
    }

    @Test
    void limitIgnoresCoursesCreditsParentDoesntCount() {
        CompletionResult r = evaluator.evaluate(cscLimit(true, creditsIn(0.5, "HPS")),
                taken("HPS100H1", "CSC108H1", "CSC148H1"));

        assertEquals(MET, status(r, 2));
        assertEquals(MET, status(r, 1));
    }

    @Test
    void limitCountsMaybeMatches() {
        CreditsReq cap = new CreditsReq(0, 0.0, null, CourseFilter.builder().lists(List.of("Unknown")).build(), false);
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, hpsOrPermission(), "raw"),
                completionRow(2, RowKind.LIMIT, null, 1, cap, "raw")));

        CompletionResult r = evaluator.evaluate(c, taken("HPS100H1"));

        assertEquals(REVIEW, status(r, 2));
    }

    @Test
    void audienceMismatchSkipsRow() {
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, creditsIn(0.5, "HPS"), "raw"),
                withAudience(2, RowKind.LIMIT, 1, hpsCap()),
                withAudience(3, RowKind.ALLOCATION, 1, req("HPS999H1")),
                withAudience(4, RowKind.ALLOCATION, null, req("CSC108H1"))));

        CompletionResult r = evaluator.evaluate(c, withFaculty("Engineering", "HPS100H1"));

        for (int row = 2; row <= 4; row++) {
            RowResult skipped = r.row(row).orElseThrow();
            assertFalse(skipped.isApplicable(), "row " + row);
            assertEquals(NOT_MET, skipped.getStatus());
            assertEquals(ARTS_AND_SCIENCE, skipped.getReason().orElseThrow());
            assertEquals(NOT_MET, skipped.getDetail().orElseThrow().getStatus());
            assertEquals(List.of(), skipped.getClaimed());
        }
        assertEquals(MET, status(r, 1));
        assertEquals(MET, r.getOverall());
    }

    @Test
    void audienceMatchApplies() {
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, creditsIn(0.5, "HPS"), "raw"),
                withAudience(2, RowKind.LIMIT, 1, hpsCap())));

        CompletionResult r = evaluator.evaluate(c, withFaculty("Arts & Science", "HPS100H1"));

        assertTrue(r.row(2).orElseThrow().isApplicable());
        assertEquals(NOT_MET, status(r, 2));
        assertEquals(NOT_MET, status(r, 1));
    }

    @Test
    void audienceUnknownStillApplies() {
        Completion c = completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, creditsIn(0.5, "HPS"), "raw"),
                withAudience(2, RowKind.LIMIT, 1, hpsCap())));

        CompletionResult r = evaluator.evaluate(c, taken("HPS100H1"));

        assertTrue(r.row(2).orElseThrow().isApplicable());
        assertEquals(NOT_MET, status(r, 1));
    }

    private static CreditsReq hpsCap() {
        return new CreditsReq(0, 0.0, null, CourseFilter.builder().designators(List.of("HPS")).build(), false);
    }

    private static Requirement hpsOrPermission() {
        return or(creditsIn(0.5, "HPS"), text("or permission of the department"));
    }

    private static Completion cscLimit(boolean transcriptScope, Requirement parentReq) {
        CreditsReq cap = new CreditsReq(0, 0.5, null, CourseFilter.builder().designators(List.of("CSC")).build(),
                false);
        return completion(List.of(
                completionRow(1, RowKind.ALLOCATION, null, null, parentReq, "raw"),
                new CompletionRow(2, RowKind.LIMIT, null, null, 1, List.of(), cap, null, transcriptScope, List.of(),
                        "raw")));
    }

    private static CompletionRow withAudience(int number, RowKind kind, Integer parent, Requirement req) {
        ProfileReq audience = new ProfileReq("faculty", "Arts & Science", ARTS_AND_SCIENCE, false);
        return new CompletionRow(number, kind, null, null, parent, List.of(), req, audience, false, List.of(), "raw");
    }

    private static AcademicSnapshot withFaculty(String faculty, String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder().profileFact("faculty", faculty);
        for (String code : codes) {
            b.taken(record(code));
        }
        return b.build();
    }
}
