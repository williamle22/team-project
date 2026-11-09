package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnrolmentKindTest {

    @Test
    void fromStringRoundTrips() {
        for (EnrolmentKind e : EnrolmentKind.values()) {
            assertEquals(e, EnrolmentKind.fromString(e.getValue()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> EnrolmentKind.fromString("unknown"));
    }
}
