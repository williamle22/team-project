package entity.catalog;

import entity.requirement.CourseReq;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequisiteTest {

    @Test
    void nullReqRequiresNothing() {
        Requisite r = new Requisite(null, List.of("NA"), "NA");
        assertTrue(r.requiresNothing());
        assertTrue(r.getReq().isEmpty());
        assertEquals(List.of("NA"), r.getNotes());
    }

    @Test
    void rejectsNullRaw() {
        assertThrows(NullPointerException.class, () -> new Requisite(null, List.of(), null));
    }

    @Test
    void equalByValue() {
        assertEquals(new Requisite(null, List.of(), "NA"), new Requisite(null, List.of(), "NA"));
        assertNotEquals(new Requisite(null, List.of(), "NA"), new Requisite(null, List.of(), "other"));
    }

    @Test
    void gettersReturnConstructorValues() {
        CourseReq csc108 = new CourseReq("CSC108H1", null, false);
        Requisite r = new Requisite(csc108, List.of("CSC108H1 or equivalent"), "CSC108H1");
        assertFalse(r.requiresNothing());
        assertEquals(csc108, r.getReq().orElseThrow());
        assertEquals(List.of("CSC108H1 or equivalent"), r.getNotes());
        assertEquals("CSC108H1", r.getRaw());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Requisite.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesRaw() {
        assertTrue(new Requisite(null, List.of(), "CSC108H1").toString().contains("CSC108H1"));
    }
}
