package entity.timetable;

import entity.catalog.Section;
import entity.term.Placement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.timetable.ActivityType.LEC;
import static entity.timetable.ActivityType.PRA;
import static entity.timetable.ActivityType.TUT;
import static org.junit.jupiter.api.Assertions.*;

class CourseOfferingTest {
    private static final Activity TUT0101 = new Activity("TUT0101", TUT, List.of(), List.of());
    private static final Activity PRA0101 = new Activity("PRA0101", PRA, List.of(), List.of());
    private static final Activity LEC0101 = new Activity("LEC0101", LEC, List.of(), List.of());
    private static final Activity TUT0102 = new Activity("TUT0102", TUT, List.of(), List.of());

    private static final CourseOffering CSC108 = new CourseOffering("CSC108H1", Placement.fallWinter(2026, Section.S),
            List.of(TUT0101, PRA0101, LEC0101, TUT0102));

    @Test
    void activityLooksUpByCode() {
        assertEquals(TUT0102, CSC108.activity("TUT0102").get());
        assertTrue(CSC108.activity("TUT9999").isEmpty());
    }

    @Test
    void activitiesOfFiltersByTypeInOrder() {
        assertEquals(List.of(TUT0101, TUT0102), CSC108.activitiesOf(TUT));
        assertEquals(List.of(LEC0101), CSC108.activitiesOf(LEC));
    }

    @Test
    void activitiesOfMissingTypeIsEmpty() {
        CourseOffering lectureOnly = new CourseOffering("ENG140Y1", Placement.fallWinter(2026, Section.Y),
                List.of(LEC0101));
        assertTrue(lectureOnly.activitiesOf(TUT).isEmpty());
    }

    @Test
    void activityTypesInEnumOrderWithoutDuplicates() {
        assertEquals(List.of(LEC, TUT, PRA), new ArrayList<>(CSC108.activityTypes()));
    }

    @Test
    void copiesItsList() {
        List<Activity> activities = new ArrayList<>(List.of(LEC0101));
        CourseOffering offering = new CourseOffering("CSC108H1", Placement.fallWinter(2026, Section.S), activities);
        activities.add(TUT0101);
        assertEquals(List.of(LEC0101), offering.getActivities());
    }

    @Test
    void gettersReturnConstructorValues() {
        assertEquals("CSC108H1", CSC108.getCourseCode());
        assertEquals(Placement.fallWinter(2026, Section.S), CSC108.getPlacement());
        assertEquals(List.of(TUT0101, PRA0101, LEC0101, TUT0102), CSC108.getActivities());
    }

    @Test
    void toStringIncludesCourseCode() {
        assertTrue(CSC108.toString().contains("CSC108H1"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(CourseOffering.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
