package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RowKindTest {

    @Test
    void fromStringRoundTrips() {
        for (RowKind r : RowKind.values()) {
            assertEquals(r, RowKind.fromString(r.getValue()));
        }
    }

    @Test
    void fromStringRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> RowKind.fromString("unknown"));
    }
}
