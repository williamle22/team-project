package interface_adapter.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseEligibilityViewTest {
    private static final UUID ID = UUID.randomUUID();
    private static final EligibilityLineView LINE = new EligibilityLineView("Prerequisites", "met", "", List.of());

    @Test
    void gettersReturnConstructorValues() {
        CourseEligibilityView v = new CourseEligibilityView(ID, "CSC148H1", "not met", "Missing CSC108H1",
                List.of(LINE), List.of("Not offered in Winter 2027"));

        assertEquals(ID, v.getPlannedCourseId());
        assertEquals("CSC148H1", v.getCode());
        assertEquals("not met", v.getStatus());
        assertEquals("Missing CSC108H1", v.getSummary());
        assertEquals(List.of(LINE), v.getLines());
        assertEquals(List.of("Not offered in Winter 2027"), v.getWarnings());
    }

    @Test
    void copiesLists() {
        List<EligibilityLineView> lines = new ArrayList<>(List.of(LINE));
        List<String> warnings = new ArrayList<>(List.of("a"));
        CourseEligibilityView v = new CourseEligibilityView(ID, "CSC148H1", "met", "", lines, warnings);

        lines.clear();
        warnings.clear();

        assertEquals(List.of(LINE), v.getLines());
        assertEquals(List.of("a"), v.getWarnings());
        assertThrows(UnsupportedOperationException.class, () -> v.getLines().add(LINE));
        assertThrows(UnsupportedOperationException.class, () -> v.getWarnings().add("b"));
    }

    @Test
    void rejectsNulls() {
        List<EligibilityLineView> l = List.of();
        List<String> w = List.of();

        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(null, "a", "met", "", l, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, null, "met", "", l, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", null, "", l, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", "met", null, l, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", "met", "", null, w));
        assertThrows(NullPointerException.class, () -> new CourseEligibilityView(ID, "a", "met", "", l, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CourseEligibilityView.class)
                .withPrefabValues(EligibilityLineView.class, LINE,
                        new EligibilityLineView("b", "review", "", List.of()))
                .withNonnullFields("plannedCourseId", "code", "status", "summary", "lines", "warnings")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new CourseEligibilityView(ID, "CSC148H1", "not met", "Missing CSC108H1", List.of(LINE),
                List.of("Not offered in Winter 2027")).toString();

        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("not met"));
        assertTrue(s.contains("Missing CSC108H1"));
        assertTrue(s.contains("Prerequisites"));
        assertTrue(s.contains("Not offered in Winter 2027"));
    }
}
