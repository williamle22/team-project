package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.ExclusionItem;
import entity.requirement.TextKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static entity.evaluation.ExclusionResult.EXCLUDED;
import static entity.evaluation.ExclusionResult.NOT_EXCLUDED;
import static entity.evaluation.ExclusionResult.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.excludesCourses;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.withExclusions;

class ExclusionCheckerTest {

    private static ExclusionItem concurrentWith(String code) {
        return new ExclusionItem(List.of(code), List.of(), List.of(), List.of(), true, null, null, null);
    }

    private static ExclusionItem noted(String note, String... codes) {
        return new ExclusionItem(List.of(codes), List.of(), List.of(), List.of(), false, note, null, null);
    }

    private static ExclusionItem filter(String designator, List<Integer> levels, String... exclude) {
        return new ExclusionItem(List.of(), List.of(designator), levels, List.of(exclude), false, null, null, null);
    }

    private static ExclusionItem notedFilter(String designator, String note) {
        return new ExclusionItem(List.of(), List.of(designator), List.of(), List.of(), false, note, null, null);
    }

    private static ExclusionItem text(String text) {
        return new ExclusionItem(List.of(), List.of(), List.of(), List.of(), false, null, TextKind.PLACEMENT, text);
    }

    private static ExclusionReason reason(String text, ExclusionResult result) {
        return new ExclusionReason(text, result);
    }

    private static AcademicSnapshot taken(String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder();
        for (String code : codes) {
            b.taken(record(code));
        }
        return b.build();
    }

    private static AcademicSnapshot concurrent(String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder();
        for (String code : codes) {
            b.concurrent(record(code));
        }
        return b.build();
    }

    @Test
    void noExclusionsIsNotExcluded() {
        ExclusionOutcome o = ExclusionChecker.check(course("CSC148H1"), taken("CSC111H1"));

        assertEquals(new ExclusionOutcome(NOT_EXCLUDED, List.of(), List.of()), o);
    }

    @Test
    void courseSetTaken() {
        Course c = withExclusions("CSC148H1", excludesCourses("CSC111H1"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken("CSC111H1"));

        assertEquals(EXCLUDED, o.getResult());
        assertEquals(List.of(reason("excluded by CSC111H1", EXCLUDED)), o.getReasons());
        assertEquals(List.of("CSC111H1"), o.getBlockingCourses());
    }

    @Test
    void courseSetNotTaken() {
        Course c = withExclusions("CSC148H1", excludesCourses("CSC111H1"));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("CSC108H1")).getResult());
    }

    @Test
    void multiCourseSetNeedsAll() {
        Course c = withExclusions("BCH210H1", excludesCourses("CHM361H5", "CHM362H5"));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("CHM361H5")).getResult());

        ExclusionOutcome both = ExclusionChecker.check(c, taken("CHM361H5", "CHM362H5"));
        assertEquals(EXCLUDED, both.getResult());
        assertEquals(List.of(reason("excluded by CHM361H5 and CHM362H5", EXCLUDED)), both.getReasons());
        assertEquals(List.of("CHM361H5", "CHM362H5"), both.getBlockingCourses());
    }

    @Test
    void plainCourseSetIgnoresConcurrentCourses() {
        Course c = withExclusions("CSC148H1", excludesCourses("CSC111H1"));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, concurrent("CSC111H1")).getResult());
    }

    @Test
    void concurrentCourseSet() {
        Course c = withExclusions("AST430H1", concurrentWith("AST425Y1"));

        ExclusionOutcome o = ExclusionChecker.check(c, concurrent("AST425Y1"));
        assertEquals(EXCLUDED, o.getResult());
        assertEquals(List.of(reason("can't be taken at the same time as AST425Y1", EXCLUDED)), o.getReasons());
        assertEquals(List.of("AST425Y1"), o.getBlockingCourses());

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("AST425Y1")).getResult());
    }

    @Test
    void courseSetWithNoteIsReview() {
        Course c = withExclusions("AFR365H1", noted("(Special Topics) offered in Fall 2019", "NEW357H1"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken("NEW357H1"));

        assertEquals(REVIEW, o.getResult());
        assertEquals(List.of(reason("may be excluded by NEW357H1 (Special Topics) offered in Fall 2019", REVIEW)),
                o.getReasons());
        assertTrue(o.getBlockingCourses().isEmpty());
    }

    @Test
    void filterMatches() {
        Course c = withExclusions("PHY100H1", filter("PHY", List.of(), "PHY196H1"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken("CSC148H1", "PHY131H1"));

        assertEquals(EXCLUDED, o.getResult());
        assertEquals(List.of(reason("excluded by PHY131H1", EXCLUDED)), o.getReasons());
        assertEquals(List.of("PHY131H1"), o.getBlockingCourses());
    }

    @Test
    void filterExcludedCourseDoesntCount() {
        Course c = withExclusions("PHY100H1", filter("PHY", List.of(), "PHY196H1"));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("PHY196H1")).getResult());
    }

    @Test
    void filterChecksLevels() {
        Course c = withExclusions("FSL322H1", filter("FSL", List.of(400)));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("FSL321H1")).getResult());
        assertEquals(EXCLUDED, ExclusionChecker.check(c, taken("FSL420H1")).getResult());
    }

    @Test
    void filterAnyLevelWhenEmpty() {
        Course c = withExclusions("PHY100H1", filter("PHY", List.of()));

        assertEquals(EXCLUDED, ExclusionChecker.check(c, taken("PHY407H1")).getResult());
    }

    @Test
    void filterSkipsTheCourseItself() {
        Course c = withExclusions("PHY100H1", filter("PHY", List.of(), "PHY196H1"));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, taken("PHY100H1")).getResult());
    }

    @Test
    void filterOnlyCountsTakenCourses() {
        Course c = withExclusions("PHY100H1", filter("PHY", List.of()));

        assertEquals(NOT_EXCLUDED, ExclusionChecker.check(c, concurrent("PHY131H1")).getResult());
    }

    @Test
    void filterWithNoteIsReview() {
        Course c = withExclusions("CSC104H1", notedFilter("CSC", "except AP, IB, CAPE or GCE Transfer Credits"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken("CSC148H1"));

        assertEquals(REVIEW, o.getResult());
        assertEquals(List.of(reason("may be excluded by CSC148H1 except AP, IB, CAPE or GCE Transfer Credits",
                REVIEW)), o.getReasons());
    }

    @Test
    void textIsReview() {
        Course c = withExclusions("FSL322H1", text("placement at the FSL400+ level"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken());

        assertEquals(REVIEW, o.getResult());
        assertEquals(List.of(reason("placement at the FSL400+ level", REVIEW)), o.getReasons());
    }

    @Test
    void excludedBeatsReview() {
        Course c = withExclusions("FSL322H1", text("placement at the FSL400+ level"), excludesCourses("FSL321Y1"));

        ExclusionOutcome o = ExclusionChecker.check(c, taken("FSL321Y1"));

        assertEquals(EXCLUDED, o.getResult());
        assertEquals(List.of(reason("placement at the FSL400+ level", REVIEW),
                reason("excluded by FSL321Y1", EXCLUDED)), o.getReasons());
        assertEquals(List.of("FSL321Y1"), o.getBlockingCourses());
    }

    @Test
    void conflictsWithinFindsPair() {
        Course csc148 = withExclusions("CSC148H1", excludesCourses("CSC111H1"));
        Course csc111 = withExclusions("CSC111H1", excludesCourses("CSC148H1"));

        Map<String, Set<String>> conflicts = ExclusionChecker.conflictsWithin(
                List.of(csc148, course("MAT137Y1"), csc111));

        assertEquals(Map.of("CSC148H1", Set.of("CSC111H1"), "CSC111H1", Set.of("CSC148H1")), conflicts);
        assertEquals(List.of("CSC148H1", "CSC111H1"), new ArrayList<>(conflicts.keySet()));
    }

    @Test
    void conflictsWithinCourseSetNeedsAll() {
        Course bch = withExclusions("BCH210H1", excludesCourses("CHM361H5", "CHM362H5"));

        assertTrue(ExclusionChecker.conflictsWithin(List.of(bch, course("CHM361H5"))).isEmpty());
        assertEquals(Map.of("BCH210H1", Set.of("CHM361H5", "CHM362H5")),
                ExclusionChecker.conflictsWithin(List.of(bch, course("CHM361H5"), course("CHM362H5"))));
    }

    @Test
    void conflictsWithinFilterSkipsItself() {
        Course phy100 = withExclusions("PHY100H1", filter("PHY", List.of(), "PHY196H1"));

        Map<String, Set<String>> conflicts = ExclusionChecker.conflictsWithin(
                List.of(phy100, course("PHY131H1"), course("PHY196H1"), course("CSC148H1")));

        assertEquals(Map.of("PHY100H1", Set.of("PHY131H1")), conflicts);
    }

    @Test
    void conflictsWithinSkipsTextNotedAndConcurrentItems() {
        Course c = withExclusions("AST430H1", text("permission"), noted("(UTM)", "AST425Y1"),
                concurrentWith("AST425Y1"));

        assertTrue(ExclusionChecker.conflictsWithin(List.of(c, course("AST425Y1"))).isEmpty());
    }
}
