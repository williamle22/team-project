package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrReqTest {

    private static final CourseReq A = new CourseReq("PHY131H1", null, false);
    private static final CourseReq B = new CourseReq("PHY151H1", null, false);

    @Test
    void gettersReturnConstructorValues() {
        OrReq r = new OrReq(List.of(A, B), true);
        assertEquals(List.of(A, B), r.getItems());
        assertTrue(r.isConcurrent());
    }

    @Test
    void copiesItems() {
        List<Requirement> items = new ArrayList<>(List.of(A));
        OrReq r = new OrReq(items, false);
        items.add(B);
        assertEquals(List.of(A), r.getItems());
    }

    @Test
    void rejectsNullItems() {
        assertThrows(NullPointerException.class, () -> new OrReq(null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new OrReq(List.of(A, B), false), new OrReq(List.of(A, B), false));
        assertEquals(new OrReq(List.of(A, B), false).hashCode(), new OrReq(List.of(A, B), false).hashCode());
        assertNotEquals(new OrReq(List.of(A, B), false), new OrReq(List.of(A, B), true));
    }

    @Test
    void notEqualToAndWithSameItems() {
        assertNotEquals(new OrReq(List.of(A, B), false), new AndReq(List.of(A, B), null, false));
    }
}
