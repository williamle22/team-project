package entity.catalog;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {

    @Test
    void phy152IsWinterOnly() {
        Sessions s = new Sessions(List.of(Section.S), List.of());
        assertTrue(s.offeredInWinter());
        assertFalse(s.offeredInFall());
        assertFalse(s.offeredInSummer());
        assertFalse(s.isFullYear());
    }

    @Test
    void fullYearIsBothTerms() {
        Sessions s = new Sessions(List.of(Section.Y), List.of());
        assertTrue(s.offeredInFall());
        assertTrue(s.offeredInWinter());
        assertTrue(s.isFullYear());
    }

    @Test
    void offersChecksTheRightPeriod() {
        Sessions s = new Sessions(List.of(), List.of(Section.F));
        assertTrue(s.offers(SessionPeriod.SUMMER, Section.F));
        assertFalse(s.offers(SessionPeriod.FALL_WINTER, Section.F));
    }

    @Test
    void yPlacementNeedsY() {
        Sessions s = new Sessions(List.of(Section.F, Section.S), List.of());
        assertFalse(s.offers(SessionPeriod.FALL_WINTER, Section.Y));
    }

    @Test
    void copiesItsLists() {
        List<Section> fallWinter = new ArrayList<>(List.of(Section.F));
        Sessions s = new Sessions(fallWinter, List.of());
        fallWinter.add(Section.S);
        assertEquals(List.of(Section.F), s.getFallWinter());
        assertThrows(UnsupportedOperationException.class, () -> s.getFallWinter().add(Section.Y));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new Sessions(null, List.of()));
    }

    @Test
    void equalByValue() {
        assertEquals(new Sessions(List.of(Section.S), List.of()), new Sessions(List.of(Section.S), List.of()));
    }
}