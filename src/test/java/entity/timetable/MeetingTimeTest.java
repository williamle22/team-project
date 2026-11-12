package entity.timetable;

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
}
