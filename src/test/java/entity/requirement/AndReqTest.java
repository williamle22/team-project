package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AndReqTest {

    private static final CourseReq A = new CourseReq("CSC148H1", null, false);
    private static final CourseReq B = new CourseReq("CSC165H1", null, false);

    @Test
    void gettersReturnConstructorValues() {
        AndReq r = new AndReq(List.of(A, B), 0.5, true);
        assertEquals(List.of(A, B), r.getItems());
        assertEquals(0.5, r.getWeight().orElseThrow());
        assertTrue(r.isConcurrent());
        assertTrue(new AndReq(List.of(A), null, false).getWeight().isEmpty());
    }

    @Test
    void copiesItems() {
        List<Requirement> items = new ArrayList<>(List.of(A));
        AndReq r = new AndReq(items, null, false);
        items.add(B);
        assertEquals(List.of(A), r.getItems());
    }

    @Test
    void rejectsNullItems() {
        assertThrows(NullPointerException.class, () -> new AndReq(null, null, false));
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new AndReq(List.of(A, B), -0.5, false));
    }

    @Test
    void coursePairNeedsWeightAndCourses() {
        assertTrue(new AndReq(List.of(A, B), 1.0, false).isCoursePair());
        assertFalse(new AndReq(List.of(A, B), null, false).isCoursePair());
        assertFalse(new AndReq(List.of(A, new OrReq(List.of(B), false)), 1.0, false).isCoursePair());
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new AndReq(List.of(A, B), 0.5, false), new AndReq(List.of(A, B), 0.5, false));
        assertEquals(new AndReq(List.of(A, B), 0.5, false).hashCode(), new AndReq(List.of(A, B), 0.5, false).hashCode());
        assertNotEquals(new AndReq(List.of(A, B), 0.5, false), new AndReq(List.of(A, B), 0.5, true));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(AndReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShowsItemsWeightAndConcurrent() {
        String s = new AndReq(List.of(A, B), 0.5, true).toString();
        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("weight 0.5"));
        assertTrue(s.contains("concurrent"));
    }

    @Test
    void toStringOmitsAbsentWeightAndConcurrent() {
        String s = new AndReq(List.of(A, B), null, false).toString();
        assertTrue(s.contains("CSC148H1"));
        assertFalse(s.contains("weight"));
        assertFalse(s.contains("concurrent"));
    }

    @Test
    void rejectsNonFiniteWeight() {
        assertThrows(IllegalArgumentException.class, () -> new AndReq(List.of(A, B), Double.NaN, false));
    }
}
