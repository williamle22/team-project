package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotReqTest {

    private static final CourseReq A = new CourseReq("CSC207H1", null, false);

    @Test
    void gettersReturnConstructorValues() {
        NotReq r = new NotReq(A, true);
        assertEquals(A, r.getItem());
        assertTrue(r.isConcurrent());
    }

    @Test
    void rejectsNullItem() {
        assertThrows(NullPointerException.class, () -> new NotReq(null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new NotReq(A, false), new NotReq(A, false));
        assertEquals(new NotReq(A, false).hashCode(), new NotReq(A, false).hashCode());
        assertNotEquals(new NotReq(A, false), new NotReq(A, true));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(NotReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }
}
