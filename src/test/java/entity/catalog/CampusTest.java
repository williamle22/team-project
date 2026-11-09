package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CampusTest {

    @Test
    void fromStringRoundTrips() {
        for (Campus c : Campus.values()) {
            assertEquals(c, Campus.fromString(c.getValue()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> Campus.fromString("unknown"));
    }
}
