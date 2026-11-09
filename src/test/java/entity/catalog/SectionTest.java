package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void fromStringRoundTrips() {
        for (Section v : Section.values()) {
            assertEquals(v, Section.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(Section.F, Section.fromString("F"));
        assertEquals(Section.S, Section.fromString("S"));
        assertEquals(Section.Y, Section.fromString("Y"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> Section.fromString("unknown"));
    }
}