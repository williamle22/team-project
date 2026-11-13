package entity.timetable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static entity.term.AcademicTerm.fall;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledMeetingTest {
    private static final MeetingTime MONDAY = new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(13, 0),
            LocalTime.of(14, 0), "PB");

    @Test
    void gettersReturnConstructorValues() {
        ScheduledMeeting m = new ScheduledMeeting("CSC148H1", "TUT0101", ActivityType.TUT, MONDAY);
        assertEquals("CSC148H1", m.getCourseCode());
        assertEquals("TUT0101", m.getActivityCode());
        assertEquals(ActivityType.TUT, m.getType());
        assertEquals(MONDAY, m.getTime());
    }

    @Test
    void toStringIncludesCourseAndActivity() {
        String s = new ScheduledMeeting("CSC148H1", "TUT0101", ActivityType.TUT, MONDAY).toString();
        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("TUT0101"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ScheduledMeeting.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
