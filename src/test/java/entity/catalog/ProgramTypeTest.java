package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramTypeTest {

    @Test
    void fromStringRoundTrips() {
        for (ProgramType v : ProgramType.values()) {
            assertEquals(v, ProgramType.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(ProgramType.SPECIALIST, ProgramType.fromString("Specialist"));
        assertEquals(ProgramType.MAJOR, ProgramType.fromString("Major"));
        assertEquals(ProgramType.MINOR, ProgramType.fromString("Minor"));
        assertEquals(ProgramType.FOCUS, ProgramType.fromString("Focus"));
        assertEquals(ProgramType.CERTIFICATE, ProgramType.fromString("Certificate"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ProgramType.fromString("unknown"));
    }
}