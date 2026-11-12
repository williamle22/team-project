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
    void numbersMatchBackend() {
        assertEquals(BreadthCategory.CREATIVE_CULTURAL, BreadthCategory.fromInt(1));
        assertEquals(BreadthCategory.THOUGHT_BELIEF, BreadthCategory.fromInt(2));
        assertEquals(BreadthCategory.SOCIETY_INSTITUTIONS, BreadthCategory.fromInt(3));
        assertEquals(BreadthCategory.LIVING_THINGS, BreadthCategory.fromInt(4));
        assertEquals(BreadthCategory.PHYSICAL_MATHEMATICAL, BreadthCategory.fromInt(5));
        assertThrows(IllegalArgumentException.class, () -> BreadthCategory.fromInt(6));
    }

    @Test
    void toStringFormat() {
        assertEquals("BR 1: Creative and Cultural Representations", BreadthCategory.CREATIVE_CULTURAL.toString());
    }

    @Test
    void displayNameIsCalendarWording() {
        assertEquals("The Physical and Mathematical Universes", BreadthCategory.PHYSICAL_MATHEMATICAL.getDisplayName());
    }
}
