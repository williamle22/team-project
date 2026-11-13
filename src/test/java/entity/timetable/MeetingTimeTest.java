package entity.timetable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static entity.term.AcademicTerm.fall;
import static entity.term.AcademicTerm.winter;
import static org.junit.jupiter.api.Assertions.*;

class MeetingTimeTest {

    private static MeetingTime fallMonday(int startHour, int endHour) {
        return new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), null);
    }

    @Test
    void overlapping() {
        assertTrue(fallMonday(10, 12).overlaps(fallMonday(11, 13)));
        assertTrue(fallMonday(11, 13).overlaps(fallMonday(10, 12)));
        assertTrue(fallMonday(10, 13).overlaps(fallMonday(11, 12)));
    }

    @Test
    void backToBackDoesntOverlap() {
        assertFalse(fallMonday(10, 11).overlaps(fallMonday(11, 12)));
    }

    @Test
    void differentDaysDontOverlap() {
        MeetingTime tuesday = new MeetingTime(fall(2026), DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), null);
        assertFalse(fallMonday(10, 12).overlaps(tuesday));
    }

    @Test
    void differentTermsDontOverlap() {
        MeetingTime winter = new MeetingTime(winter(2027), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0), null);
        assertFalse(fallMonday(10, 12).overlaps(winter));
    }

    @Test
    void rejectsEndNotAfterStart() {
        assertThrows(IllegalArgumentException.class, () -> fallMonday(12, 10));
        assertThrows(IllegalArgumentException.class, () -> fallMonday(10, 10));
    }

    @Test
    void gettersReturnConstructorValues() {
        MeetingTime m = new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "BA1130");
        assertEquals(fall(2026), m.getTerm());
        assertEquals(DayOfWeek.MONDAY, m.getDay());
        assertEquals(LocalTime.of(10, 0), m.getStart());
        assertEquals(LocalTime.of(11, 0), m.getEnd());
        assertEquals("BA1130", m.getLocation().orElseThrow());
    }

    @Test
    void missingLocationIsEmpty() {
        assertTrue(fallMonday(10, 11).getLocation().isEmpty());
    }

    @Test
    void laterMeetingDoesntOverlapEarlierOne() {
        assertFalse(fallMonday(12, 13).overlaps(fallMonday(10, 11)));
    }

    @Test
    void toStringIncludesLocationOnlyWhenPresent() {
        MeetingTime located = new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "BA1130");
        assertTrue(located.toString().contains("BA1130"));
        assertTrue(fallMonday(10, 11).toString().endsWith("11:00}"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(MeetingTime.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
