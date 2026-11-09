package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnrolmentKindTest {

    @Test
    void fromStringRoundTrips() {
        for (EnrolmentKind v : EnrolmentKind.values()) {
            assertEquals(v, EnrolmentKind.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(EnrolmentKind.OPEN, EnrolmentKind.fromString("open"));
        assertEquals(EnrolmentKind.LIMITED, EnrolmentKind.fromString("limited"));
        assertEquals(EnrolmentKind.FOCUS, EnrolmentKind.fromString("focus"));
        assertEquals(EnrolmentKind.SUSPENDED, EnrolmentKind.fromString("suspended"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> EnrolmentKind.fromString("unknown"));
    }
}