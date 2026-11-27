package entity.evaluation;

import entity.catalog.BreadthCategory;
import entity.requirement.CourseFilter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static entity.evaluation.Match.MAYBE;
import static entity.evaluation.Match.NO;
import static entity.evaluation.Match.YES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.withBreadth;

class CourseMatcherTest {

    private static final CourseRecord CSC148 = record("CSC148H1", 75);
    private static final CourseRecord MAT237 = record("MAT237Y1", 80);
    private static final CourseRecord MAT1 = CourseRecord.unspecifiedTransfer("MAT1**", 2.0).orElseThrow();

    private static Match match(CourseFilter.Builder f, CourseRecord r) {
        return CourseMatcher.matches(f.build(), r, MembershipResolver.NONE);
    }

    private static Match match(CourseFilter.Builder f, CourseRecord r, MembershipResolver lists) {
        return CourseMatcher.matches(f.build(), r, lists);
    }

    private static final class FakeResolver implements MembershipResolver {
        private final Map<String, Set<String>> lists;
        private final Map<Integer, Set<String>> rows;

        FakeResolver(Map<String, Set<String>> lists, Map<Integer, Set<String>> rows) {
            this.lists = lists;
            this.rows = rows;
        }

        @Override
        public Match onList(String listName, CourseRecord record) {
            return lists.getOrDefault(listName, Set.of()).contains(record.getCode()) ? YES : NO;
        }

        @Override
        public Match inRowPool(int rowNumber, CourseRecord record) {
            return rows.getOrDefault(rowNumber, Set.of()).contains(record.getCode()) ? YES : NO;
        }

        @Override
        public Optional<Status> rowStatus(int rowNumber) {
            return Optional.empty();
        }
    }

    @Test
    void noCoursesOrDesignatorsMatchesAnything() {
        assertEquals(YES, match(CourseFilter.builder(), CSC148));
        assertEquals(YES, CourseMatcher.matches(CourseFilter.any(), MAT237, MembershipResolver.NONE));
    }

    @Test
    void coursesPasses() {
        assertEquals(YES, match(CourseFilter.builder().courses(List.of("MAT137Y1", "CSC148H1")), CSC148));
    }

    @Test
    void coursesFails() {
        assertEquals(NO, match(CourseFilter.builder().courses(List.of("CSC108H1")), CSC148));
    }

    @Test
    void designatorsPasses() {
        assertEquals(YES, match(CourseFilter.builder().designators(List.of("CSC")), CSC148));
    }

    @Test
    void designatorsFails() {
        assertEquals(NO, match(CourseFilter.builder().designators(List.of("MAT")), CSC148));
    }

    @Test
    void coursesOrDesignators() {
        CourseFilter.Builder f = CourseFilter.builder().courses(List.of("CSC148H1")).designators(List.of("MAT"));

        assertEquals(YES, match(f, CSC148));
        assertEquals(YES, match(f, MAT237));
        assertEquals(NO, match(f, record("CSC207H1")));
    }

    @Test
    void levelsPasses() {
        assertEquals(YES, match(CourseFilter.builder().levels(List.of(100, 200)), CSC148));
    }

    @Test
    void levelsFails() {
        assertEquals(NO, match(CourseFilter.builder().levels(List.of(300, 400)), CSC148));
    }

    @Test
    void breadthPasses() {
        CourseRecord r = record(withBreadth("CSC148H1", BreadthCategory.THOUGHT_BELIEF,
                BreadthCategory.PHYSICAL_MATHEMATICAL), 75);

        assertEquals(YES, match(CourseFilter.builder().breadth(List.of(4, 5)), r));
    }

    @Test
    void breadthFails() {
        CourseRecord r = record(withBreadth("CSC148H1", BreadthCategory.PHYSICAL_MATHEMATICAL), 75);

        assertEquals(NO, match(CourseFilter.builder().breadth(List.of(1, 2)), r));
        assertEquals(NO, match(CourseFilter.builder().breadth(List.of(5)), CSC148));
    }

    @Test
    void excludePasses() {
        assertEquals(YES, match(CourseFilter.builder().exclude(List.of("CSC108H1")), CSC148));
    }

    @Test
    void excludeFails() {
        assertEquals(NO, match(CourseFilter.builder().exclude(List.of("CSC148H1")), CSC148));
    }

    @Test
    void excludeDesignatorsPasses() {
        assertEquals(YES, match(CourseFilter.builder().excludeDesignators(List.of("MAT")), CSC148));
    }

    @Test
    void excludeDesignatorsFails() {
        assertEquals(NO, match(CourseFilter.builder().excludeDesignators(List.of("CSC")), CSC148));
    }

    @Test
    void maxCourseAmountPasses() {
        assertEquals(YES, match(CourseFilter.builder().maxCourseAmount(0.5), CSC148));
    }

    @Test
    void maxCourseAmountFails() {
        assertEquals(NO, match(CourseFilter.builder().maxCourseAmount(0.5), MAT237));
    }

    @Test
    void minGradePasses() {
        assertEquals(YES, match(CourseFilter.builder().minGrade(75), CSC148));
    }

    @Test
    void minGradeFails() {
        assertEquals(NO, match(CourseFilter.builder().minGrade(76), CSC148));
    }

    @Test
    void minGradeUnknownIsMaybe() {
        assertEquals(MAYBE, match(CourseFilter.builder().minGrade(60), record("CSC148H1")));
    }

    @Test
    void unknownGradeWithoutMinGradeIsYes() {
        assertEquals(YES, match(CourseFilter.builder(), record("CSC148H1")));
    }

    @Test
    void transferPasses() {
        CourseRecord transfer = new CourseRecord(course("CSC148H1"), null, true, false);

        assertEquals(YES, match(CourseFilter.builder().transfer(true), transfer));
    }

    @Test
    void transferFails() {
        assertEquals(NO, match(CourseFilter.builder().transfer(true), CSC148));
    }

    @Test
    void crNcrPasses() {
        CourseRecord crNcr = new CourseRecord(course("CSC148H1"), null, false, true);

        assertEquals(YES, match(CourseFilter.builder().crNcr(true), crNcr));
    }

    @Test
    void crNcrFails() {
        assertEquals(NO, match(CourseFilter.builder().crNcr(true), CSC148));
    }

    @Test
    void listsPasses() {
        FakeResolver lists = new FakeResolver(Map.of("Group 2", Set.of("CSC148H1")), Map.of());

        assertEquals(YES, match(CourseFilter.builder().lists(List.of("Group 1", "Group 2")), CSC148, lists));
    }

    @Test
    void listsFails() {
        FakeResolver lists = new FakeResolver(Map.of("Group 2", Set.of("MAT237Y1")), Map.of());

        assertEquals(NO, match(CourseFilter.builder().lists(List.of("Group 1", "Group 2")), CSC148, lists));
    }

    @Test
    void requirementsPasses() {
        FakeResolver rows = new FakeResolver(Map.of(), Map.of(3, Set.of("CSC148H1")));

        assertEquals(YES, match(CourseFilter.builder().requirements(List.of(1, 3)), CSC148, rows));
    }

    @Test
    void requirementsFails() {
        FakeResolver rows = new FakeResolver(Map.of(), Map.of(3, Set.of("MAT237Y1")));

        assertEquals(NO, match(CourseFilter.builder().requirements(List.of(1, 3)), CSC148, rows));
    }

    @Test
    void noneResolverMakesListsMaybe() {
        assertEquals(MAYBE, match(CourseFilter.builder().lists(List.of("Group 1")), CSC148));
        assertEquals(MAYBE, match(CourseFilter.builder().requirements(List.of(1)), CSC148));
    }

    @Test
    void designatorAndLevel() {
        CourseFilter.Builder f = CourseFilter.builder().designators(List.of("CSC")).levels(List.of(300, 400));

        assertEquals(YES, match(f, record("CSC373H1")));
        assertEquals(NO, match(f, CSC148));
        assertEquals(NO, match(f, record("MAT337H1")));
    }

    @Test
    void designatorButExcluded() {
        CourseFilter.Builder f = CourseFilter.builder().designators(List.of("CSC")).exclude(List.of("CSC148H1"));

        assertEquals(NO, match(f, CSC148));
        assertEquals(YES, match(f, record("CSC207H1")));
    }

    @Test
    void failedCheckBeatsUnknownGrade() {
        CourseFilter.Builder f = CourseFilter.builder().designators(List.of("MAT")).minGrade(60);

        assertEquals(NO, match(f, record("CSC148H1")));
    }

    @Test
    void unspecifiedTransferMaybeMatchesDesignatorOrCourse() {
        assertEquals(MAYBE, match(CourseFilter.builder().designators(List.of("MAT")), MAT1));
        assertEquals(MAYBE, match(CourseFilter.builder().courses(List.of("MAT135H1")), MAT1));
        assertEquals(NO, match(CourseFilter.builder().courses(List.of("MAT235H1")), MAT1));
    }

    @Test
    void unspecifiedTransferMaybeMatchesBreadth() {
        assertEquals(MAYBE, match(CourseFilter.builder().breadth(List.of(5)), MAT1));
    }

    @Test
    void unspecifiedTransferCountsAsAnyCourse() {
        assertEquals(YES, match(CourseFilter.builder(), MAT1));
        assertEquals(YES, match(CourseFilter.builder().levels(List.of(100)), MAT1));
    }

    @Test
    void maxCourseAmountUsesRecordWeight() {
        assertEquals(NO, match(CourseFilter.builder().maxCourseAmount(1.0), MAT1));
    }
}
