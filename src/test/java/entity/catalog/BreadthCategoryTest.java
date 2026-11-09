package entity.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BreadthCategoryTest {

    @Test
    void fromIntRoundTrips() {
        for (BreadthCategory b : BreadthCategory.values()) {
            assertEquals(b, BreadthCategory.fromInt(b.getNumber()));
        }
    }

    @Test
    void fromIntRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> BreadthCategory.fromInt(0));
    }

    @Test
    void toStringFormat() {
        assertEquals("BR 1: Creative and Cultural Representations", BreadthCategory.CREATIVE_CULTURAL.toString());
    }
}
