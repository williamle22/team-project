package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramTypeTest {

    @Test
    void fromStringRoundTrips() {
        for (ProgramType p : ProgramType.values()) {
            assertEquals(p, ProgramType.fromString(p.getProgramType()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ProgramType.fromString("unknown"));
    }
}
