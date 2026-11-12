package entity.term;

import entity.catalog.Section;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.summer;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class PlacementTest {

    @Test
    void fallHalf() {
        Placement p = Placement.fallWinter(2026, Section.F);
        assertEquals(fall(2026), p.startTerm());
        assertEquals(fall(2026), p.endTerm());
        assertEquals(List.of(fall(2026)), p.terms());
        assertEquals(2026, p.academicYear());
    }

    @Test
    void winterHalf() {
        Placement p = Placement.fallWinter(2026, Section.S);
        assertEquals(winter(2027), p.startTerm());
        assertEquals(winter(2027), p.endTerm());
        assertEquals(List.of(winter(2027)), p.terms());
        assertEquals(2026, p.academicYear());
    }

    @Test
    void fullYear() {
        Placement p = Placement.fallWinter(2026, Section.Y);
        assertEquals(fall(2026), p.startTerm());
        assertEquals(winter(2027), p.endTerm());
        assertEquals(List.of(fall(2026), winter(2027)), p.terms());
        assertEquals(2026, p.academicYear());
    }

    @Test
    void summerBelongsToPreviousAcademicYear() {
        Placement p = Placement.summer(2027, Section.F);
        assertEquals(summer(2027), p.startTerm());
        assertEquals(summer(2027), p.endTerm());
        assertEquals(List.of(summer(2027)), p.terms());
        assertEquals(2026, p.academicYear());
    }

    @Test
    void yOccupiesBothTermsButNotSummer() {
        Placement p = Placement.fallWinter(2026, Section.Y);
        assertTrue(p.occupies(fall(2026)));
        assertTrue(p.occupies(winter(2027)));
        assertFalse(p.occupies(summer(2027)));
        assertFalse(p.occupies(summer(2026)));
    }

    @Test
    void halfCoursesOccupyOnlyTheirTerm() {
        Placement f = Placement.fallWinter(2026, Section.F);
        Placement s = Placement.fallWinter(2026, Section.S);
        assertTrue(f.occupies(fall(2026)));
        assertFalse(f.occupies(winter(2027)));
        assertTrue(s.occupies(winter(2027)));
        assertFalse(s.occupies(fall(2026)));
    }

    @Test
    void summerOccupiesOnlySummer() {
        Placement p = Placement.summer(2027, Section.Y);
        assertTrue(p.occupies(summer(2027)));
        assertFalse(p.occupies(winter(2027)));
        assertFalse(p.occupies(fall(2027)));
    }

    @Test
    void halfCourseInFallUsesSameYear() {
        assertEquals(Placement.fallWinter(2026, Section.F), Placement.halfCourseIn(fall(2026)));
    }

    @Test
    void halfCourseInWinterUsesPreviousFallYear() {
        assertEquals(Placement.fallWinter(2026, Section.S), Placement.halfCourseIn(winter(2027)));
    }

    @Test
    void halfCourseInSummerThrows() {
        assertThrows(IllegalArgumentException.class, () -> Placement.halfCourseIn(summer(2027)));
    }

    @Test
    void toStringForEachShape() {
        assertEquals("Fall 2026", Placement.fallWinter(2026, Section.F).toString());
        assertEquals("Winter 2027", Placement.fallWinter(2026, Section.S).toString());
        assertEquals("Fall-Winter 2026-2027 (Y)", Placement.fallWinter(2026, Section.Y).toString());
        assertEquals("Summer 2027 (F)", Placement.summer(2027, Section.F).toString());
    }
}
