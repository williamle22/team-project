package entity.catalog;

import entity.requirement.CourseFilter;
import entity.requirement.CourseReq;
import entity.requirement.CreditsReq;
import entity.requirement.ProgramReq;
import entity.requirement.Requirement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompletionTest {

    private static Requirement credits(double amount, String list) {
        return new CreditsReq(amount, null, null, CourseFilter.builder().lists(List.of(list)).build(), false);
    }

    private static CompletionRow row(int number, RowKind kind, Integer parent, Requirement req) {
        return new CompletionRow(number, kind, null, null, parent, List.of(), req, null, false, List.of(), "");
    }

    private static final CompletionRow ROW_1 = row(1, RowKind.ALLOCATION, null, credits(4.0, "Group 2"));
    private static final CompletionRow ROW_2 = row(2, RowKind.CONSTRAINT, 1, credits(1.0, "Group 2"));
    private static final CompletionRow ROW_3 = row(3, RowKind.ALLOCATION, 1, credits(1.0, "Group 2"));
    private static final CompletionRow ROW_4 = row(4, RowKind.ALLOCATION, 1, credits(2.0, "Requirement 4 courses"));

    private static final CourseList GROUP_2 = new CourseList("Group 2", null,
            List.of(new CourseReq("HPS100H1", null, false), new CourseReq("HPS110H1", null, false)));
    private static final CourseList REQ_4 = new CourseList("Requirement 4 courses", "Requirement 4",
            List.of(new CourseReq("HPS200H1", null, false)));

    private static final Completion ASMIN1001 = new Completion(List.of(ROW_1, ROW_2, ROW_3, ROW_4),
            List.of(GROUP_2, REQ_4), List.of(), List.of(), "");

    @Test
    void rowLookup() {
        assertEquals(ROW_3, ASMIN1001.row(3).orElseThrow());
        assertTrue(ASMIN1001.row(99).isEmpty());
    }

    @Test
    void childrenOfRowOne() {
        assertEquals(List.of(ROW_2, ROW_3, ROW_4), ASMIN1001.childrenOf(1));
        assertTrue(ASMIN1001.childrenOf(4).isEmpty());
    }

    @Test
    void topLevelRows() {
        assertEquals(List.of(ROW_1), ASMIN1001.topLevelRows());
    }

    @Test
    void listLookup() {
        assertEquals(REQ_4, ASMIN1001.list("Requirement 4 courses").orElseThrow());
        assertTrue(ASMIN1001.list("Group 9").isEmpty());
    }

    @Test
    void duplicateRowNumberThrows() {
        CompletionRow anotherRow3 = row(3, RowKind.LIMIT, 1, credits(0.5, "Group 2"));
        assertThrows(IllegalArgumentException.class,
                () -> new Completion(List.of(ROW_1, ROW_3, anotherRow3), List.of(), List.of(), List.of(), ""));
    }

    @Test
    void duplicateListNameThrows() {
        CourseList anotherGroup2 = new CourseList("Group 2", "Again", List.of());
        assertThrows(IllegalArgumentException.class,
                () -> new Completion(List.of(ROW_1), List.of(GROUP_2, anotherGroup2), List.of(), List.of(), ""));
    }

    @Test
    void danglingParentIsAllowed() {
        CompletionRow orphan = row(5, RowKind.ALLOCATION, 42, credits(1.0, "Group 2"));
        Completion c = new Completion(List.of(ROW_1, orphan), List.of(), List.of(), List.of(), "");
        assertEquals(List.of(orphan), c.childrenOf(42));
        assertEquals(List.of(ROW_1), c.topLevelRows());
    }

    @Test
    void gettersReturnConstructorValues() {
        Requirement excluded = new ProgramReq(List.of("ASMAJ1001"), "ASMAJ1001", false);
        Completion c = new Completion(List.of(ROW_1), List.of(GROUP_2), List.of(excluded), List.of("note"), "raw");
        assertEquals(List.of(ROW_1), c.getRequirements());
        assertEquals(List.of(GROUP_2), c.getLists());
        assertEquals(List.of(excluded), c.getExclusions());
        assertEquals(List.of("note"), c.getNotes());
        assertEquals("raw", c.getRaw());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Completion.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesLists() {
        assertTrue(ASMIN1001.toString().contains("Requirement 4 courses"));
    }
}
