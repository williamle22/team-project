package entity.timetable;

import entity.catalog.Section;
import entity.term.Placement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledCourseTest {
    private static final CourseOffering CSC148 = new CourseOffering("CSC148H1", Placement.fallWinter(2026, Section.F),
            List.of(new Activity("LEC0101", ActivityType.LEC, List.of(), List.of())));
    private static final TimetableChoice CHOICE = TimetableChoice.empty().with(ActivityType.LEC, "LEC0101");

    @Test
    void gettersReturnConstructorValues() {
        ScheduledCourse c = new ScheduledCourse(CSC148, CHOICE);
        assertEquals(CSC148, c.getOffering());
        assertEquals(CHOICE, c.getChoice());
    }

    @Test
    void toStringIncludesCourseCodeAndChoice() {
        String s = new ScheduledCourse(CSC148, CHOICE).toString();
        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("LEC0101"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ScheduledCourse.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
