package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void fromStringRoundTrips() {
        for (Section s : Section.values()) {
            assertEquals(s, Section.fromString(s.getValue()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> Section.fromString("unknown"));
    }
}
