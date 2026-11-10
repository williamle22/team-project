package entity.requirement;

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
}
