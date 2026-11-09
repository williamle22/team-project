package entity.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExclusionsTest {

    @Test
    void emptyRawIsAllowed() {
        Exclusions e = new Exclusions(List.of(), List.of(), "");
        assertEquals("", e.getRaw());
        assertTrue(e.getItems().isEmpty());
    }

    @Test
    void itemsAreReadOnly() {
        Exclusions e = new Exclusions(List.of(), List.of(), "");
        assertThrows(UnsupportedOperationException.class, () -> e.getItems().add(null));
    }
}