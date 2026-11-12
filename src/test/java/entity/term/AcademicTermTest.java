package entity.term;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.summer;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class AcademicTermTest {

    @Test
    void ordersWithinAYear() {
        assertTrue(winter(2027).isBefore(summer(2027)));
        assertTrue(summer(2027).isBefore(fall(2027)));
        assertTrue(fall(2027).isAfter(winter(2027)));
    }

    @Test
    void ordersAcrossYears() {
        assertTrue(fall(2026).isBefore(winter(2027)));
    }

    @Test
    void nextWrapsTheYear() {
        assertEquals(summer(2027), winter(2027).next());
        assertEquals(fall(2027), summer(2027).next());
        assertEquals(winter(2027), fall(2026).next());
    }

    @Test
    void previousWrapsTheYear() {
        assertEquals(summer(2026), fall(2026).previous());
        assertEquals(winter(2026), summer(2026).previous());
        assertEquals(fall(2026), winter(2027).previous());
    }

    @Test
    void sortsAList() {
        List<AcademicTerm> terms = new ArrayList<>(List.of(fall(2027), winter(2027), fall(2026), summer(2027)));
        Collections.sort(terms);
        assertEquals(List.of(fall(2026), winter(2027), summer(2027), fall(2027)), terms);
    }

    @Test
    void toStringIsReadable() {
        assertEquals("Fall 2026", fall(2026).toString());
        assertEquals("Winter 2027", winter(2027).toString());
    }
}
