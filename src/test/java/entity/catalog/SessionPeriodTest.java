package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionPeriodTest {

    @Test
    void fromStringRoundTrips() {
        for (SessionPeriod v : SessionPeriod.values()) {
            assertEquals(v, SessionPeriod.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(SessionPeriod.FALL_WINTER, SessionPeriod.fromString("fall_winter"));
        assertEquals(SessionPeriod.SUMMER, SessionPeriod.fromString("summer"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> SessionPeriod.fromString("unknown"));
    }
}