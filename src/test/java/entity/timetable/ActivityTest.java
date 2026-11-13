package entity.timetable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static entity.term.AcademicTerm.fall;
import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {

    @Test
    void copiesItsLists() {
        MeetingTime monday = new MeetingTime(fall(2026), DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), null);
        List<MeetingTime> meetings = new ArrayList<>(List.of(monday));
        List<String> instructors = new ArrayList<>(List.of("Dash"));
        Activity a = new Activity("LEC0101", ActivityType.LEC, meetings, instructors);
        meetings.clear();
        instructors.add("Liu");
        assertEquals(List.of(monday), a.getMeetings());
        assertEquals(List.of("Dash"), a.getInstructors());
    }

    @Test
    void gettersReturnConstructorValues() {
        Activity a = new Activity("TUT0101", ActivityType.TUT, List.of(), List.of("Dash"));
        assertEquals("TUT0101", a.getCode());
        assertEquals(ActivityType.TUT, a.getType());
        assertEquals(List.of(), a.getMeetings());
        assertEquals(List.of("Dash"), a.getInstructors());
    }

    @Test
    void toStringIncludesCode() {
        assertTrue(new Activity("LEC0101", ActivityType.LEC, List.of(), List.of()).toString().contains("LEC0101"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Activity.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
