package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampusTest {

    @Test
    void fromStringRoundTrips() {
        for (Campus v : Campus.values()) {
            assertEquals(v, Campus.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(Campus.ONLINE, Campus.fromString("online"));
        assertEquals(Campus.UTSG, Campus.fromString("utsg"));
        assertEquals(Campus.UTSC, Campus.fromString("utsc"));
        assertEquals(Campus.UTM, Campus.fromString("utm"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> Campus.fromString("unknown"));
    }
}