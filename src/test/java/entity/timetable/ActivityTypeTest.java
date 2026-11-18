package entity.timetable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTypeTest {

    @Test
    void fromStringRoundTrips() {
        for (ActivityType v : ActivityType.values()) {
            assertEquals(v, ActivityType.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(ActivityType.LEC, ActivityType.fromString("LEC"));
        assertEquals(ActivityType.TUT, ActivityType.fromString("TUT"));
        assertEquals(ActivityType.PRA, ActivityType.fromString("PRA"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ActivityType.fromString("Lecture"));
    }
}
