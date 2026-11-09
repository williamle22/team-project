package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextKindTest {

    @Test
    void fromStringRoundTrips() {
        for (TextKind t : TextKind.values()) {
            assertEquals(t, TextKind.fromString(t.getTextKind()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> TextKind.fromString("unknown"));
    }
}
