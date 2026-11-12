package entity.timetable;

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
}
