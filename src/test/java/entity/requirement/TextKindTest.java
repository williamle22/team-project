package entity.requirement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextKindTest {

    @Test
    void fromStringRoundTrips() {
        for (TextKind v : TextKind.values()) {
            assertEquals(v, TextKind.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(TextKind.PERMISSION, TextKind.fromString("permission"));
        assertEquals(TextKind.MEMBERSHIP, TextKind.fromString("membership"));
        assertEquals(TextKind.PLACEMENT, TextKind.fromString("placement"));
        assertEquals(TextKind.PREPARATION, TextKind.fromString("preparation"));
        assertEquals(TextKind.ADMISSION, TextKind.fromString("admission"));
        assertEquals(TextKind.AMBIGUITY, TextKind.fromString("ambiguity"));
        assertEquals(TextKind.CONTENT, TextKind.fromString("content"));
        assertEquals(TextKind.OTHER, TextKind.fromString("other"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> TextKind.fromString("unknown"));
    }
}