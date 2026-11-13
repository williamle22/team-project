package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChooseReqTest {

    private static final CourseReq A = new CourseReq("MAT137Y1", null, false);
    private static final CourseReq B = new CourseReq("MAT157Y1", null, false);

    @Test
    void gettersReturnConstructorValues() {
        ChooseReq r = new ChooseReq(1, List.of(A, B), true);
        assertEquals(1, r.getCount());
        assertEquals(List.of(A, B), r.getItems());
        assertTrue(r.isConcurrent());
    }

    @Test
    void copiesItems() {
        List<Requirement> items = new ArrayList<>(List.of(A));
        ChooseReq r = new ChooseReq(1, items, false);
        items.add(B);
        assertEquals(List.of(A), r.getItems());
    }

    @Test
    void rejectsNullItems() {
        assertThrows(NullPointerException.class, () -> new ChooseReq(1, null, false));
    }

    @Test
    void rejectsZeroCount() {
        assertThrows(IllegalArgumentException.class, () -> new ChooseReq(0, List.of(A, B), false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new ChooseReq(1, List.of(A, B), false), new ChooseReq(1, List.of(A, B), false));
        assertEquals(new ChooseReq(1, List.of(A, B), false).hashCode(), new ChooseReq(1, List.of(A, B), false).hashCode());
        assertNotEquals(new ChooseReq(1, List.of(A, B), false), new ChooseReq(1, List.of(A, B), true));
        assertNotEquals(new ChooseReq(1, List.of(A, B), false), new ChooseReq(2, List.of(A, B), false));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ChooseReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShowsCountItemsAndConcurrent() {
        assertTrue(new ChooseReq(1, List.of(A, B), true).toString().contains("concurrent"));
        String s = new ChooseReq(1, List.of(A, B), false).toString();
        assertTrue(s.contains("1 of"));
        assertTrue(s.contains("MAT137Y1"));
        assertFalse(s.contains("concurrent"));
    }
}
