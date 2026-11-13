package entity.timetable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static entity.term.AcademicTerm.fall;
import static org.junit.jupiter.api.Assertions.*;

class ConflictTest {
    private static final MeetingTime MONDAY = new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(13, 0),
            LocalTime.of(14, 0), null);
    private static final ScheduledMeeting LECTURE = new ScheduledMeeting("CSC148H1", "LEC0101", ActivityType.LEC, MONDAY);
    private static final ScheduledMeeting TUTORIAL = new ScheduledMeeting("CSC148H1", "TUT0101", ActivityType.TUT, MONDAY);

    @Test
    void gettersReturnConstructorValues() {
        Conflict c = new Conflict(LECTURE, TUTORIAL);
        assertEquals(LECTURE, c.getFirst());
        assertEquals(TUTORIAL, c.getSecond());
        assertEquals(fall(2026), c.getTerm());
    }

    @Test
    void toStringIncludesBothActivities() {
        String s = new Conflict(LECTURE, TUTORIAL).toString();
        assertTrue(s.contains("LEC0101"));
        assertTrue(s.contains("TUT0101"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Conflict.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
