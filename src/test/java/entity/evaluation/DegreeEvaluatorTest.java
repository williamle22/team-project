package entity.evaluation;

import entity.catalog.ProgramType;
import entity.catalog.RowKind;
import entity.degree.Degree;
import entity.degree.DegreeType;
import entity.degree.ProgramCombinationRule;
import entity.requirement.CourseFilter;
import entity.requirement.Requirement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static entity.catalog.ProgramType.SPECIALIST;
import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.completionRow;
import static testutil.CatalogFixtures.credits;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.testDegree;
import static testutil.CatalogFixtures.text;

class DegreeEvaluatorTest {
    private static final String[] THREE_CREDITS = {
            "CSC108H1", "CSC148H1", "CSC207H1", "CSC236H1", "CSC369H1", "CSC373H1"};
    private static final ProgramCombinationRule HONOURS = new ProgramCombinationRule(List.of(
            Map.of(SPECIALIST, 1), Map.of(MAJOR, 2), Map.of(MAJOR, 1, MINOR, 2)));
    private static final Degree HBSC = new Degree(DegreeType.HBSC, testDegree().getCompletion(), HONOURS, List.of());
    private static final Degree BCOM = new Degree(DegreeType.BCOM, testDegree().getCompletion(),
            new ProgramCombinationRule(List.of(Map.of(SPECIALIST, 1))), List.of("ASSPE2038", "ASSPE2676"));
    private static final EnrolmentResult ENROLMENT = new EnrolmentResult(MET, "meets", null, List.of());
    private static final String HONOURS_NEEDS = "needs 1 Specialist, or 2 Majors, or 1 Major and 2 Minors completed";
    private static final String SHARED = "your programs share courses, so they might not include 12.0 different "
            + "credits";
    private static final String NEEDS_REVIEW =
            "met if your programs needing review are complete and you are admitted to them";

    private final DegreeEvaluator evaluator = new DegreeEvaluator(new CompletionEvaluator());

    private static String[] courses(String designator, int from, int to) {
        List<String> codes = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            codes.add(designator + (100 + i) + "Y1");
        }
        return codes.toArray(new String[0]);
    }

    private static String[] join(String[]... groups) {
        List<String> codes = new ArrayList<>();
        for (String[] g : groups) {
            codes.addAll(List.of(g));
        }
        return codes.toArray(new String[0]);
    }

    private static AcademicSnapshot snapshot(String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder();
        for (String code : codes) {
            b.taken(record(code));
        }
        for (String program : List.of("SPE", "MAJ1", "MAJ2", "MIN1", "MIN2", "ASSPE2676", "ASSPE9999")) {
            b.enrolledProgram(program);
        }
        return b.build();
    }

    private static RowResult row(int number, RowKind kind, Integer parent, boolean applicable, Requirement req,
                                 String... claimed) {
        EvaluationNode detail = req == null ? null : EvaluationNode.leaf(req, MET, null, List.of(claimed));
        return new RowResult(completionRow(number, kind, null, parent, req, "raw"), applicable, MET, null, detail,
                List.of(claimed));
    }

    private static ProgramProgress program(String code, ProgramType type, Status status, List<RowResult> rows) {
        return new ProgramProgress(code, type, ENROLMENT, new CompletionResult(rows, status));
    }

    private static ProgramProgress program(String code, ProgramType type, Status status, Requirement req,
                                           String... claimed) {
        return program(code, type, status, List.of(row(1, RowKind.ALLOCATION, null, true, req, claimed)));
    }

    private static ProgramProgress major(String code, double minimum, String... claimed) {
        return program(code, MAJOR, MET, credits(minimum, CourseFilter.any()), claimed);
    }

    private DegreeProgress evaluate(Degree degree, AcademicSnapshot snapshot, ProgramProgress... programs) {
        return evaluator.evaluate(degree, snapshot, List.of(programs));
    }

    @Test
    void degreeRowsMet() {
        DegreeProgress p = evaluate(HBSC, snapshot(THREE_CREDITS), program("SPE", SPECIALIST, MET, text("t")));

        assertEquals(DegreeType.HBSC, p.getType());
        assertEquals(MET, p.getCompletion().getOverall());
        assertEquals(MET, p.getProgramCombination());
        assertTrue(p.getProgramCombinationReason().isEmpty());
        assertEquals(MET, p.getOverall());
    }

    @Test
    void notEnoughCredits() {
        DegreeProgress p = evaluate(HBSC, snapshot("CSC108H1", "CSC148H1", "CSC207H1", "CSC369H1", "CSC373H1"),
                program("SPE", SPECIALIST, MET, text("t")));

        assertEquals(NOT_MET, p.getCompletion().getOverall());
        assertEquals(MET, p.getProgramCombination());
        assertEquals(NOT_MET, p.getOverall());
    }

    @Test
    void tooMuchFirstYear() {
        DegreeProgress p = evaluate(HBSC,
                snapshot("CSC108H1", "CSC148H1", "CSC165H1", "CSC207H1", "CSC369H1", "CSC373H1"),
                program("SPE", SPECIALIST, MET, text("t")));

        assertEquals(NOT_MET, p.getCompletion().row(3).orElseThrow().getStatus());
        assertEquals(NOT_MET, p.getOverall());
    }

    @Test
    void specialistAloneIsMet() {
        DegreeProgress p = evaluate(HBSC, snapshot(), program("SPE", SPECIALIST, MET, text("t")));

        assertEquals(MET, p.getProgramCombination());
    }

    @Test
    void specialistSkipsTheDifferentCreditsCheck() {
        String[] a = courses("AAA", 1, 7);

        DegreeProgress p = evaluate(HBSC, snapshot(a), major("MAJ1", 7, a), major("MAJ2", 7, a),
                program("SPE", SPECIALIST, MET, text("t")));

        assertEquals(MET, p.getProgramCombination());
    }

    @Test
    void distinctSubjectMajorsAreMet() {
        String[] a = courses("AAA", 1, 7);
        String[] b = courses("BBB", 1, 7);

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b)), major("MAJ1", 7, a), major("MAJ2", 7, b));

        assertEquals(MET, p.getProgramCombination());
        assertTrue(p.getProgramCombinationReason().isEmpty());
    }

    @Test
    void sharedCoursesMetWhenClaimsReachTwelve() {
        String[] a = courses("AAA", 1, 8);
        String[] b = join(courses("AAA", 1, 3), courses("BBB", 1, 5));

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b)), major("MAJ1", 7, a), major("MAJ2", 7, b));

        assertEquals(MET, p.getProgramCombination());
    }

    @Test
    void sharedClaimsThatCouldBeSwappedAreReview() {
        String[] a = courses("AAA", 1, 7);
        String[] b = join(courses("AAA", 1, 3), courses("BBB", 1, 4));
        String[] couldUse = join(b, courses("BBB", 5, 5));
        EvaluationNode detail = EvaluationNode.leaf(credits(7, CourseFilter.any()), MET, null, List.of(couldUse));
        ProgramProgress major2 = program("MAJ2", MAJOR, MET, List.of(new RowResult(
                completionRow(1, RowKind.ALLOCATION, null, null, credits(7, CourseFilter.any()), "raw"), true, MET,
                null, detail, List.of(b))));

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, couldUse)), major("MAJ1", 7, a), major2);

        assertEquals(REVIEW, p.getProgramCombination());
        assertEquals(SHARED, p.getProgramCombinationReason().orElseThrow());
    }

    @Test
    void sharedCoursesUnderTwelveAreNotMet() {
        String[] a = courses("AAA", 1, 7);
        String[] b = join(courses("AAA", 1, 3), courses("BBB", 1, 4));
        String[] unclaimed = courses("ZZZ", 1, 3);

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b, unclaimed)), major("MAJ1", 7, a),
                major("MAJ2", 7, b));

        assertEquals(NOT_MET, p.getProgramCombination());
        assertEquals("your programs include only 11.0 different credits; they need 12.0",
                p.getProgramCombinationReason().orElseThrow());
    }

    @Test
    void courseClaimedTwiceByOneProgramCountsOnce() {
        String[] a = courses("AAA", 1, 7);
        String[] b = courses("BBB", 1, 7);
        ProgramProgress twice = program("MAJ1", MAJOR, MET, List.of(
                row(1, RowKind.ALLOCATION, null, true, credits(7, CourseFilter.any()), a),
                row(2, RowKind.CONSTRAINT, 1, true, credits(1, CourseFilter.any()), a)));

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b)), twice, major("MAJ2", 7, b));

        assertEquals(MET, p.getProgramCombination());
    }

    @Test
    void reviewProgramGivesReview() {
        String[] a = courses("AAA", 1, 7);
        String[] b = courses("BBB", 1, 7);

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b)), major("MAJ1", 7, a),
                program("MAJ2", MAJOR, REVIEW, credits(7, CourseFilter.any()), b));

        assertEquals(REVIEW, p.getProgramCombination());
        assertEquals(NEEDS_REVIEW, p.getProgramCombinationReason().orElseThrow());
    }

    @Test
    void intendedCompleteProgramGivesReview() {
        DegreeProgress p = evaluate(HBSC, snapshot(), program("INTENDED", SPECIALIST, MET, text("t")));

        assertEquals(REVIEW, p.getProgramCombination());
        assertEquals(NEEDS_REVIEW, p.getProgramCombinationReason().orElseThrow());
    }

    @Test
    void incompleteProgramsDontCount() {
        String[] a = courses("AAA", 1, 7);
        String[] b = courses("BBB", 1, 7);

        DegreeProgress p = evaluate(HBSC, snapshot(join(a, b)), major("MAJ1", 7, a),
                program("MAJ2", MAJOR, NOT_MET, credits(7, CourseFilter.any()), b));

        assertEquals(NOT_MET, p.getProgramCombination());
        assertEquals(HONOURS_NEEDS, p.getProgramCombinationReason().orElseThrow());
    }

    @Test
    void bcomCountsANamedSpecialist() {
        DegreeProgress p = evaluate(BCOM, snapshot(), program("ASSPE2676", SPECIALIST, MET, text("t")));

        assertEquals(MET, p.getProgramCombination());
    }

    @Test
    void bcomIgnoresOtherSpecialistsAndNamesThePrograms() {
        DegreeProgress p = evaluate(BCOM, snapshot(), program("ASSPE9999", SPECIALIST, MET, text("t")));

        assertEquals(NOT_MET, p.getProgramCombination());
        assertEquals("needs 1 Specialist completed (one of ASSPE2038, ASSPE2676)",
                p.getProgramCombinationReason().orElseThrow());
    }
}
