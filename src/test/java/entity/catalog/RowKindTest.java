package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowKindTest {

    @Test
    void fromStringRoundTrips() {
        for (RowKind v : RowKind.values()) {
            assertEquals(v, RowKind.fromString(v.getValue()));
        }
    }

    @Test
    void parsesBackendStrings() {
        assertEquals(RowKind.ALLOCATION, RowKind.fromString("allocation"));
        assertEquals(RowKind.CONSTRAINT, RowKind.fromString("constraint"));
        assertEquals(RowKind.LIMIT, RowKind.fromString("limit"));
        assertEquals(RowKind.CHOICE, RowKind.fromString("choice"));
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> RowKind.fromString("unknown"));
    }
}