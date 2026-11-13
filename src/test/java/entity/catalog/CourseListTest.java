package entity.catalog;

import entity.requirement.CourseReq;
import entity.requirement.Requirement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseListTest {

    private static final CourseReq HPS100 = new CourseReq("HPS100H1", null, false);

    @Test
    void gettersReturnConstructorValues() {
        CourseList list = new CourseList("Requirement 4 courses", "Requirement 4", List.of(HPS100));
        assertEquals("Requirement 4 courses", list.getName());
        assertEquals("Requirement 4", list.getTitle().orElseThrow());
        assertEquals(List.<Requirement>of(HPS100), list.getItems());
    }

    @Test
    void titleIsOptional() {
        assertTrue(new CourseList("Group 2", null, List.of(HPS100)).getTitle().isEmpty());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(CourseList.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesName() {
        assertTrue(new CourseList("Group 2", null, List.of(HPS100)).toString().contains("Group 2"));
    }
}
