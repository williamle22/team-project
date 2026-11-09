package entity.catalog;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SessionsTest {

    @Test
    void phy152IsWinterOnly() {
        Sessions s = new Sessions(Set.of(Section.S), Set.of());
        assertTrue(s.offeredInWinter());
        assertFalse(s.offeredInFall());
        assertFalse(s.offeredInSummer());
        assertFalse(s.isFullYear());
    }

    @Test
    void fullYearIsBothTerms() {
        Sessions s = new Sessions(Set.of(Section.Y), Set.of());
        assertTrue(s.offeredInFall());
        assertTrue(s.offeredInWinter());
        assertTrue(s.isFullYear());
    }

    @Test
    void offersChecksTheRightPeriod() {
        Sessions s = new Sessions(Set.of(), Set.of(Section.F));
        assertTrue(s.offers(SessionPeriod.SUMMER, Section.F));
        assertFalse(s.offers(SessionPeriod.FALL_WINTER, Section.F));
    }

    @Test
    void yPlacementNeedsY() {
        Sessions s = new Sessions(Set.of(Section.F, Section.S), Set.of());
        assertFalse(s.offers(SessionPeriod.FALL_WINTER, Section.Y));
    }

    @Test
    void copiesItsSets() {
        Set<Section> fallWinter = new HashSet<>(Set.of(Section.F));
        Sessions s = new Sessions(fallWinter, Set.of());
        fallWinter.add(Section.S);
        assertEquals(Set.of(Section.F), s.getFallWinter());
        assertThrows(UnsupportedOperationException.class, () -> s.getFallWinter().add(Section.Y));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new Sessions(null, Set.of()));
    }

    @Test
    void equalByValue() {
        assertEquals(new Sessions(Set.of(Section.S), Set.of()), new Sessions(Set.of(Section.S), Set.of()));
    }
}