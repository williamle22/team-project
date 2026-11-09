package entity.catalog;

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
}