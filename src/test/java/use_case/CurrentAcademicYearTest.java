package use_case;

import entity.catalog.Section;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentAcademicYearTest {
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");

    private static CurrentAcademicYear at(String instant) {
        return new CurrentAcademicYear(Clock.fixed(Instant.parse(instant), TORONTO));
    }

    @Test
    void octoberFallWinterIsCurrent() {
        CurrentAcademicYear year = at("2026-10-15T12:00:00Z");

        assertTrue(year.isCurrent(Placement.fallWinter(2026, Section.F)));
        assertTrue(year.isCurrent(Placement.fallWinter(2026, Section.S)));
        assertTrue(year.isCurrent(Placement.fallWinter(2026, Section.Y)));
        assertFalse(year.isCurrent(Placement.fallWinter(2027, Section.F)));
    }

    @Test
    void februaryFallWinterIsCurrent() {
        CurrentAcademicYear year = at("2027-02-01T12:00:00Z");

        assertTrue(year.isCurrent(Placement.fallWinter(2026, Section.F)));
        assertTrue(year.isCurrent(Placement.fallWinter(2026, Section.S)));
        assertFalse(year.isCurrent(Placement.fallWinter(2027, Section.F)));
    }

    @Test
    void februaryNextSummerIsNot() {
        assertFalse(at("2027-02-01T12:00:00Z").isCurrent(Placement.summer(2027, Section.F)));
    }

    @Test
    void juneSummerIsCurrent() {
        CurrentAcademicYear year = at("2027-06-15T12:00:00Z");

        assertTrue(year.isCurrent(Placement.summer(2027, Section.F)));
        assertTrue(year.isCurrent(Placement.summer(2027, Section.S)));
        assertFalse(year.isCurrent(Placement.summer(2026, Section.F)));
    }

    @Test
    void juneLastFallWinterIsNot() {
        assertFalse(at("2027-06-15T12:00:00Z").isCurrent(Placement.fallWinter(2026, Section.S)));
    }

    @Test
    void juneNextFallWinterIsNot() {
        assertFalse(at("2027-06-15T12:00:00Z").isCurrent(Placement.fallWinter(2027, Section.F)));
    }

    @Test
    void septemberBoundary() {
        CurrentAcademicYear august31 = at("2026-09-01T03:59:59Z");
        CurrentAcademicYear september1 = at("2026-09-01T04:00:00Z");

        assertTrue(august31.isCurrent(Placement.summer(2026, Section.S)));
        assertFalse(august31.isCurrent(Placement.fallWinter(2026, Section.F)));
        assertFalse(september1.isCurrent(Placement.summer(2026, Section.S)));
        assertTrue(september1.isCurrent(Placement.fallWinter(2026, Section.F)));
    }

    @Test
    void mayBoundary() {
        CurrentAcademicYear april30 = at("2027-05-01T03:59:59Z");
        CurrentAcademicYear may1 = at("2027-05-01T04:00:00Z");

        assertTrue(april30.isCurrent(Placement.fallWinter(2026, Section.S)));
        assertFalse(april30.isCurrent(Placement.summer(2027, Section.F)));
        assertFalse(may1.isCurrent(Placement.fallWinter(2026, Section.S)));
        assertTrue(may1.isCurrent(Placement.summer(2027, Section.F)));
    }

    @Test
    void rejectsNullClock() {
        assertThrows(NullPointerException.class, () -> new CurrentAcademicYear(null));
    }
}
