package entity.evaluation;

import entity.catalog.Completion;
import entity.catalog.CourseList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static entity.evaluation.Match.MAYBE;
import static entity.evaluation.Match.NO;
import static entity.evaluation.Match.YES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.CatalogFixtures.and;
import static testutil.CatalogFixtures.asmin1001;
import static testutil.CatalogFixtures.completion;
import static testutil.CatalogFixtures.fromCourses;
import static testutil.CatalogFixtures.fromLists;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.req;
import static testutil.CatalogFixtures.text;

class CompletionResolverTest {
    private static final Completion ASMIN1001 = asmin1001();

    private static CompletionResolver resolver(Completion completion, CourseRecord... taken) {
        return new CompletionResolver(completion, AcademicSnapshot.builder().taken(taken).build());
    }

    @Test
    void onDirectList() {
        CompletionResolver r = resolver(ASMIN1001);

        assertEquals(YES, r.onList("Group 1", record("PHL380H1")));
        assertEquals(NO, r.onList("Group 1", record("PHL281H1")));
    }

    @Test
    void onNestedList() {
        CompletionResolver r = resolver(ASMIN1001);

        assertEquals(YES, r.onList("Requirement 4 courses", record("PHL271H1")));
        assertEquals(YES, r.onList("Requirement 4 courses", record("PHL265H1")));
        assertEquals(NO, r.onList("Requirement 4 courses", record("PHL380H1")));
    }

    @Test
    void unknownListIsMaybe() {
        assertEquals(MAYBE, resolver(ASMIN1001).onList("Group 9", record("PHL380H1")));
    }

    @Test
    void listCycleIsMaybe() {
        Completion cycle = completion(List.of(),
                new CourseList("A", null, List.of(fromLists(0, "B"), fromCourses("PHL380H1"))),
                new CourseList("B", null, List.of(fromLists(0, "A"))));
        CompletionResolver r = resolver(cycle);

        assertEquals(MAYBE, r.onList("A", record("PHL281H1")));
        assertEquals(YES, r.onList("B", record("PHL380H1")));
        assertEquals(YES, r.onList("A", record("PHL380H1")));
    }

    @Test
    void coursePairNeedsBoth() {
        Completion pairs = completion(List.of(),
                new CourseList("Portuguese", null, List.of(and(req("PRT101H1"), req("PRT102H1")))));

        assertEquals(YES, resolver(pairs, record("PRT101H1"), record("PRT102H1"))
                .onList("Portuguese", record("PRT101H1")));
        assertEquals(NO, resolver(pairs, record("PRT101H1")).onList("Portuguese", record("PRT101H1")));
        assertEquals(NO, resolver(pairs, record("PRT101H1"), record("PRT102H1"))
                .onList("Portuguese", record("PRT201H1")));
    }

    @Test
    void coursePairCountsConcurrentPartner() {
        Completion pairs = completion(List.of(),
                new CourseList("Portuguese", null, List.of(and(req("PRT101H1"), req("PRT102H1")))));
        AcademicSnapshot snapshot = AcademicSnapshot.builder().taken(record("PRT101H1"))
                .concurrent(record("PRT102H1")).build();

        assertEquals(YES, new CompletionResolver(pairs, snapshot).onList("Portuguese", record("PRT101H1")));
    }

    @Test
    void otherItemShapesAreMaybe() {
        Completion mixed = completion(List.of(),
                new CourseList("Mixed", null, List.of(and(fromLists(0, "Group 1"), text("partly text")))),
                new CourseList("Text", null, List.of(text("any course approved by the department"))));
        CompletionResolver r = resolver(mixed);

        assertEquals(MAYBE, r.onList("Mixed", record("PHL380H1")));
        assertEquals(MAYBE, r.onList("Text", record("PHL380H1")));
    }

    @Test
    void inRowPoolUsesClaimedCourses() {
        CompletionResolver r = resolver(ASMIN1001);

        assertEquals(MAYBE, r.inRowPool(3, record("PHL281H1")));

        r.record(new RowResult(ASMIN1001.row(3).orElseThrow(), true, Status.MET, null, null, List.of("PHL281H1")));

        assertEquals(YES, r.inRowPool(3, record("PHL281H1")));
        assertEquals(NO, r.inRowPool(3, record("PHL245H1")));
    }

    @Test
    void rowStatusAfterRecord() {
        CompletionResolver r = resolver(ASMIN1001);

        assertEquals(Optional.empty(), r.rowStatus(4));

        r.record(new RowResult(ASMIN1001.row(4).orElseThrow(), true, Status.NOT_MET, null, null, List.of()));

        assertEquals(Optional.of(Status.NOT_MET), r.rowStatus(4));
    }
}
