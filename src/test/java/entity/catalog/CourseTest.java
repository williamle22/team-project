package entity.catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import testutil.CatalogFixtures;

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

    @Test
    void gettersReturnConstructorValues() {
        Requisite prereqs = CatalogFixtures.requisite(CatalogFixtures.req("CSC108H1"));
        Requisite coreqs = CatalogFixtures.requisite(CatalogFixtures.req("MAT137Y1"));
        Exclusions exclusions = new Exclusions(List.of(CatalogFixtures.excludesCourses("CSC111H1")), List.of(),
                "CSC111H1");
        Sessions sessions = new Sessions(List.of(Section.F, Section.S), List.of(Section.F));
        Course c = new Course("CSC148H1", "CSC", "148", 100, 0.5, Campus.UTSG, "Introduction to Computer Science",
                "Abstract data types", prereqs, coreqs, exclusions, List.of(BreadthCategory.PHYSICAL_MATHEMATICAL),
                sessions);
        assertEquals("CSC148H1", c.getCode());
        assertEquals("CSC", c.getDesignator());
        assertEquals("148", c.getNumber());
        assertEquals(100, c.getLevel());
        assertEquals(0.5, c.getWeight());
        assertEquals(Campus.UTSG, c.getCampus());
        assertEquals("Introduction to Computer Science", c.getTitle());
        assertEquals("Abstract data types", c.getDescription());
        assertEquals(prereqs, c.getPrerequisites().orElseThrow());
        assertEquals(coreqs, c.getCorequisites().orElseThrow());
        assertEquals(exclusions, c.getExclusions().orElseThrow());
        assertEquals(List.of(BreadthCategory.PHYSICAL_MATHEMATICAL), c.getBreadth());
        assertEquals(sessions, c.getSessions().orElseThrow());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Course.class).withOnlyTheseFields("code").suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesCode() {
        assertTrue(CatalogFixtures.course("CSC148H1").toString().contains("CSC148H1"));
    }
}
