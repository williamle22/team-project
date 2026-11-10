package entity.requirement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseReqTest {

    @Test
    void gettersReturnConstructorValues() {
        CourseReq r = new CourseReq("CSC148H1", 70, true);
        assertEquals("CSC148H1", r.getCode());
        assertEquals(70, r.getMinGrade().orElseThrow());
        assertTrue(r.isConcurrent());
        assertTrue(new CourseReq("CSC148H1", null, false).getMinGrade().isEmpty());
    }

    @Test
    void rejectsNullCode() {
        assertThrows(NullPointerException.class, () -> new CourseReq(null, null, false));
    }

    @Test
    void rejectsBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> new CourseReq(" ", null, false));
    }

    @Test
    void rejectsBadMinGrade() {
        assertThrows(IllegalArgumentException.class, () -> new CourseReq("CSC148H1", 101, false));
        assertThrows(IllegalArgumentException.class, () -> new CourseReq("CSC148H1", -1, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new CourseReq("CSC148H1", 70, false), new CourseReq("CSC148H1", 70, false));
        assertEquals(new CourseReq("CSC148H1", 70, false).hashCode(), new CourseReq("CSC148H1", 70, false).hashCode());
        assertNotEquals(new CourseReq("CSC148H1", 70, false), new CourseReq("CSC148H1", 70, true));
        assertNotEquals(new CourseReq("CSC148H1", 70, false), new CourseReq("CSC148H1", null, false));
    }
}
