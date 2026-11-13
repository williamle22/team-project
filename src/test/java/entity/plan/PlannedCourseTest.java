package entity.plan;

import entity.catalog.Section;
import entity.grades.CourseGrade;
import entity.term.Placement;
import entity.timetable.ActivityType;
import entity.timetable.TimetableChoice;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlannedCourseTest {
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);

    @Test
    void createGivesDefaults() {
        PlannedCourse c = PlannedCourse.create("CSC148H1", FALL_2026);
        assertEquals("CSC148H1", c.getCode());
        assertEquals(FALL_2026, c.getPlacement());
        assertFalse(c.isCrNcr());
        assertEquals(CourseGrade.empty(), c.getGrade());
        assertTrue(c.getTimetable().isEmpty());
    }

    @Test
    void createGivesUniqueIds() {
        assertNotEquals(PlannedCourse.create("CSC148H1", FALL_2026).getId(),
                PlannedCourse.create("CSC148H1", FALL_2026).getId());
    }

    @Test
    void withMethodsKeepTheId() {
        PlannedCourse c = PlannedCourse.create("CSC148H1", FALL_2026);
        TimetableChoice choice = new TimetableChoice(Map.of(ActivityType.LEC, "LEC0101"));
        PlannedCourse changed = c.withPlacement(Placement.fallWinter(2026, Section.S))
                .withCrNcr(true)
                .withGrade(CourseGrade.empty().withFinalGrade(85))
                .withTimetable(choice);
        assertEquals(c.getId(), changed.getId());
        assertTrue(changed.isCrNcr());
        assertEquals(85, changed.getGrade().getFinalGrade().get());
        assertEquals(choice, changed.getTimetable().get());
        assertTrue(changed.withTimetable(null).getTimetable().isEmpty());
        assertEquals(FALL_2026, c.getPlacement());
    }

    @Test
    void toStringMarksCrNcrOnlyWhenSet() {
        PlannedCourse c = PlannedCourse.create("PSY100H1", FALL_2026);
        assertTrue(c.toString().contains("PSY100H1"));
        assertFalse(c.toString().contains("CR/NCR"));
        assertTrue(c.withCrNcr(true).toString().contains("CR/NCR"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(PlannedCourse.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
