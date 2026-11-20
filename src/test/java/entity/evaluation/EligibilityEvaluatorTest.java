package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.Requisite;
import entity.catalog.Section;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.concurrentReq;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.excludesCourses;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.withCoreq;
import static testutil.CatalogFixtures.withExclusions;
import static testutil.CatalogFixtures.withPrereq;
import static testutil.CatalogFixtures.withSessions;

class EligibilityEvaluatorTest {
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final AcademicSnapshot EMPTY = AcademicSnapshot.builder().build();

    private final EligibilityEvaluator evaluator = new EligibilityEvaluator();

    private static AcademicSnapshot taken(CourseRecord... records) {
        return AcademicSnapshot.builder().taken(records).build();
    }

    private static AcademicSnapshot concurrent(CourseRecord... records) {
        return AcademicSnapshot.builder().concurrent(records).build();
    }

    private EligibilityResult evaluate(Course c, AcademicSnapshot snapshot) {
        return evaluator.evaluate(c, WINTER_2027, snapshot, false);
    }

    @Test
    void prereqMetEarlier() {
        EligibilityResult r = evaluate(withPrereq("CSC207H1", req("CSC148H1")), taken(record("CSC148H1")));

        assertEquals("CSC207H1", r.getCourseCode());
        assertEquals(MET, r.getPrerequisites().getStatus());
        assertEquals(MET, r.getOverall());
    }

    @Test
    void prereqOnlyConcurrentNotMet() {
        EligibilityResult r = evaluate(withPrereq("CSC207H1", req("CSC148H1")), concurrent(record("CSC148H1")));

        assertEquals(NOT_MET, r.getPrerequisites().getStatus());
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void coreqConcurrentMet() {
        EligibilityResult r = evaluate(withCoreq("CSC207H1", req("CSC148H1")), concurrent(record("CSC148H1")));

        assertEquals(MET, r.getCorequisites().getStatus());
        assertEquals(MET, r.getOverall());
    }

    @Test
    void concurrentOnlyPrereqMetConcurrently() {
        EligibilityResult r = evaluate(withPrereq("AST430H1", concurrentReq("AST425Y1")),
                concurrent(record("AST425Y1")));

        assertEquals(MET, r.getPrerequisites().getStatus());
    }

    @Test
    void excludedCourseTaken() {
        EligibilityResult r = evaluate(withExclusions("CSC148H1", excludesCourses("CSC111H1")),
                taken(record("CSC111H1")));

        assertEquals(ExclusionResult.EXCLUDED, r.getExclusions().getResult());
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void minGradeWithoutFinalIsReview() {
        EligibilityResult r = evaluate(withPrereq("CSC207H1", req("CSC148H1", 60)), taken(record("CSC148H1")));

        assertEquals(REVIEW, r.getPrerequisites().getStatus());
        assertEquals(REVIEW, r.getOverall());
    }

    @Test
    void notOfferedThisYear() {
        EligibilityResult r = evaluator.evaluate(course("CSC207H1"), WINTER_2027, EMPTY, true);

        assertEquals(OfferingStatus.NOT_OFFERED, r.getOffering());
        assertEquals(List.of("not offered in Winter 2027"), r.getWarnings());
        assertEquals(NOT_MET, r.getOverall());
    }

    @Test
    void offeredThisYear() {
        Course c = withSessions("CSC207H1", List.of(Section.F, Section.S), List.of());

        EligibilityResult r = evaluator.evaluate(c, WINTER_2027, EMPTY, true);

        assertEquals(OfferingStatus.OFFERED, r.getOffering());
        assertTrue(r.getWarnings().isEmpty());
        assertEquals(MET, r.getOverall());
    }

    @Test
    void nextYearIgnoresOffering() {
        EligibilityResult r = evaluate(course("CSC207H1"), EMPTY);

        assertEquals(OfferingStatus.NOT_APPLICABLE, r.getOffering());
        assertTrue(r.getWarnings().isEmpty());
        assertEquals(MET, r.getOverall());
    }

    @Test
    void noRequisitesIsMet() {
        EligibilityResult r = evaluate(course("CSC108H1"), EMPTY);

        assertEquals(EvaluationNode.none("no prerequisites"), r.getPrerequisites());
        assertEquals(EvaluationNode.none("no corequisites"), r.getCorequisites());
        assertEquals(ExclusionResult.NOT_EXCLUDED, r.getExclusions().getResult());
        assertEquals(MET, r.getOverall());
    }

    @Test
    void requisiteWithoutTreeIsNone() {
        Requisite nothing = new Requisite(null, List.of(), "");
        Course c = course("CSC108H1", nothing, nothing, null, List.of(), null);

        EligibilityResult r = evaluate(c, EMPTY);

        assertEquals(EvaluationNode.none("no prerequisites"), r.getPrerequisites());
        assertEquals(EvaluationNode.none("no corequisites"), r.getCorequisites());
    }
}
