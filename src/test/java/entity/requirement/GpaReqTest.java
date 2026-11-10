package entity.requirement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GpaReqTest {

    @Test
    void gettersReturnConstructorValues() {
        GpaReq r = new GpaReq(3.0, "CHM credits", true);
        assertEquals(3.0, r.getMinGpa());
        assertEquals("CHM credits", r.getScope().orElseThrow());
        assertTrue(r.isConcurrent());
        assertTrue(new GpaReq(3.0, null, false).getScope().isEmpty());
    }

    @Test
    void rejectsBadMinGpa() {
        assertThrows(IllegalArgumentException.class, () -> new GpaReq(4.1, null, false));
        assertThrows(IllegalArgumentException.class, () -> new GpaReq(-0.1, null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new GpaReq(3.0, "CHM credits", false), new GpaReq(3.0, "CHM credits", false));
        assertEquals(new GpaReq(3.0, "CHM credits", false).hashCode(), new GpaReq(3.0, "CHM credits", false).hashCode());
        assertNotEquals(new GpaReq(3.0, "CHM credits", false), new GpaReq(3.0, "CHM credits", true));
        assertNotEquals(new GpaReq(3.0, "CHM credits", false), new GpaReq(3.0, null, false));
    }
}
