package entity.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    private static Course course(String code, String title, int level, double weight, Sessions sessions) {
        return new Course(code, "CSC", "148", level, weight, Campus.UTSG, title, "", null, null, null, List.of(),
                sessions);
    }

    @Test
    void equalityIsByCode() {
        Course a = course("CSC148H1", "Introduction to Computer Science", 100, 0.5, null);
        Course b = course("CSC148H1", "A different title", 100, 0.5, null);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, course("CSC165H1", "Introduction to Computer Science", 100, 0.5, null));
    }

    @Test
    void rejectsBadLevel() {
        assertThrows(IllegalArgumentException.class, () -> course("CSC148H1", "", 0, 0.5, null));
        assertThrows(IllegalArgumentException.class, () -> course("CSC148H1", "", 500, 0.5, null));
        assertThrows(IllegalArgumentException.class, () -> course("CSC148H1", "", 150, 0.5, null));
    }

    @Test
    void rejectsBadWeight() {
        assertThrows(IllegalArgumentException.class, () -> course("CSC148H1", "", 100, 0.25, null));
        assertThrows(IllegalArgumentException.class, () -> course("CSC148H1", "", 100, 2.0, null));
    }

    @Test
    void noSessionsMeansNotOfferedThisYear() {
        assertFalse(course("CSC148H1", "", 100, 0.5, null).isOfferedThisYear());
        assertTrue(course("CSC148H1", "", 100, 0.5, new Sessions(List.of(Section.F), List.of())).isOfferedThisYear());
    }
}
